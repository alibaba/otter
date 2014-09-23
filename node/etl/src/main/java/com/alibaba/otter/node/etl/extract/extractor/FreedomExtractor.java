/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.node.etl.extract.extractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.extract.exceptions.ExtractException;
import com.alibaba.otter.shared.common.model.config.ConfigException;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncConsistency;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncMode;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;

/**
 * 自由之门，允许手工触发数据订正，解析这些记录
 * 
 * <pre>
 * buffer表结构：
 *  id , table_id ,  type , pk_data , gmt_create , gmt_modified
 *  
 * pk_data针对多主键时，使用char(1)进行分隔
 * </pre>
 * 
 * @author jianghang 2012-4-25 下午04:41:33
 * @version 4.0.2
 */
public class FreedomExtractor extends AbstractExtractor<DbBatch> {

    private static final Logger logger    = LoggerFactory.getLogger(FreedomExtractor.class);
    private static final char   PK_SPLIT  = (char) 1;
    // private static final String ID = "id";
    private static final String TABLE_ID  = "table_id";
    private static final String FULL_NAME = "full_name";
    private static final String TYPE      = "type";
    private static final String PK_DATA   = "pk_data";

    public void extract(DbBatch dbBatch) throws ExtractException {
        Assert.notNull(dbBatch);

        // 读取配置
        Pipeline pipeline = getPipeline(dbBatch.getRowBatch().getIdentity().getPipelineId());

        boolean skipFreedom = pipeline.getParameters().getSkipFreedom();
        String bufferSchema = pipeline.getParameters().getSystemSchema();
        String bufferTable = pipeline.getParameters().getSystemBufferTable();

        List<EventData> eventDatas = dbBatch.getRowBatch().getDatas();
        Set<EventData> removeDatas = new HashSet<EventData>();// 使用set，提升remove时的查找速度
        for (EventData eventData : eventDatas) {
            if (StringUtils.equalsIgnoreCase(bufferSchema, eventData.getSchemaName())
                && StringUtils.equalsIgnoreCase(bufferTable, eventData.getTableName())) {
                if (eventData.getEventType().isDdl()) {
                    continue;
                }

                if (skipFreedom) {// 判断是否需要忽略
                    removeDatas.add(eventData);
                    continue;
                }

                // 只处理insert / update记录
                if (eventData.getEventType().isInsert() || eventData.getEventType().isUpdate()) {
                    // 重新改写一下EventData的数据，根据系统表的定义
                    EventColumn tableIdColumn = getMatchColumn(eventData.getColumns(), TABLE_ID);
                    // 获取到对应tableId的media信息
                    try {
                        DataMedia dataMedia = null;
                        Long tableId = Long.valueOf(tableIdColumn.getColumnValue());
                        eventData.setTableId(tableId);
                        if (tableId <= 0) { // 直接按照full_name进行查找
                            // 尝试直接根据schema+table name进行查找
                            EventColumn fullNameColumn = getMatchColumn(eventData.getColumns(), FULL_NAME);
                            if (fullNameColumn != null) {
                                String[] names = StringUtils.split(fullNameColumn.getColumnValue(), ".");
                                if (names.length >= 2) {
                                    dataMedia = ConfigHelper.findSourceDataMedia(pipeline, names[0], names[1]);
                                    eventData.setTableId(dataMedia.getId());
                                } else {
                                    throw new ConfigException("no such DataMedia " + names);
                                }
                            }
                        } else {
                            // 如果指定了tableId，需要按照tableId进行严格查找，如果没找到，那说明不需要进行同步
                            dataMedia = ConfigHelper.findDataMedia(pipeline,
                                Long.valueOf(tableIdColumn.getColumnValue()));
                        }

                        DbDialect dbDialect = dbDialectFactory.getDbDialect(pipeline.getId(),
                            (DbMediaSource) dataMedia.getSource());
                        // 考虑offer[1-128]的配置模式
                        if (!dataMedia.getNameMode().getMode().isSingle()
                            || !dataMedia.getNamespaceMode().getMode().isSingle()) {
                            boolean hasError = true;
                            EventColumn fullNameColumn = getMatchColumn(eventData.getColumns(), FULL_NAME);
                            if (fullNameColumn != null) {
                                String[] names = StringUtils.split(fullNameColumn.getColumnValue(), ".");
                                if (names.length >= 2) {
                                    eventData.setSchemaName(names[0]);
                                    eventData.setTableName(names[1]);
                                    hasError = false;
                                }
                            }

                            if (hasError) {
                                // 出现异常，需要记录一下
                                logger.warn("dataMedia mode:{} , fullname:{} ",
                                    dataMedia.getMode(),
                                    fullNameColumn == null ? null : fullNameColumn.getColumnValue());
                                removeDatas.add(eventData);
                                // 跳过这条记录
                                continue;
                            }
                        } else {
                            eventData.setSchemaName(dataMedia.getNamespace());
                            eventData.setTableName(dataMedia.getName());
                        }

                        // 更新业务类型
                        EventColumn typeColumn = getMatchColumn(eventData.getColumns(), TYPE);
                        EventType eventType = EventType.valuesOf(typeColumn.getColumnValue());
                        eventData.setEventType(eventType);
                        if (eventType.isUpdate()) {// 如果是update强制修改为insert，这样可以在目标端执行merge
                                                   // sql
                            eventData.setEventType(EventType.INSERT);
                        } else if (eventType.isDdl()) {
                            dbDialect.reloadTable(eventData.getSchemaName(), eventData.getTableName());
                            removeDatas.add(eventData);// 删除当前记录
                            continue;
                        }
                        // 重新构建新的业务主键字段
                        EventColumn pkDataColumn = getMatchColumn(eventData.getColumns(), PK_DATA);
                        String pkData = pkDataColumn.getColumnValue();
                        String[] pks = StringUtils.split(pkData, PK_SPLIT);

                        Table table = dbDialect.findTable(eventData.getSchemaName(), eventData.getTableName());
                        List<EventColumn> newColumns = new ArrayList<EventColumn>();
                        Column[] primaryKeyColumns = table.getPrimaryKeyColumns();
                        if (primaryKeyColumns.length > pks.length) {
                            throw new ExtractException("data pk column size not match , data:" + eventData.toString());
                        }
                        // 构建字段
                        Column[] allColumns = table.getColumns();
                        int pkIndex = 0;
                        for (int i = 0; i < allColumns.length; i++) {
                            Column column = allColumns[i];
                            if (column.isPrimaryKey()) {
                                EventColumn newColumn = new EventColumn();
                                newColumn.setIndex(i); // 设置下标
                                newColumn.setColumnName(column.getName());
                                newColumn.setColumnType(column.getTypeCode());
                                newColumn.setColumnValue(pks[pkIndex]);
                                newColumn.setKey(true);
                                newColumn.setNull(pks[pkIndex] == null);
                                newColumn.setUpdate(true);
                                // 添加到记录
                                newColumns.add(newColumn);
                                pkIndex++;
                            }
                        }
                        // 设置数据
                        eventData.setKeys(newColumns);
                        eventData.setOldKeys(new ArrayList<EventColumn>());
                        eventData.setColumns(new ArrayList<EventColumn>());
                        // 设置为行记录+反查
                        eventData.setSyncMode(SyncMode.ROW);
                        eventData.setSyncConsistency(SyncConsistency.MEDIA);
                        eventData.setRemedy(true);
                        eventData.setSize(1024);// 默认为1kb，如果还是按照binlog大小计算的话，可能会采用rpc传输，导致内存不够用
                    } catch (ConfigException e) {
                        // 忽略掉，因为系统表会被共享，所以这条记录会被不是该同步通道给获取到
                        logger.info("find DataMedia error " + eventData.toString(), e);
                        removeDatas.add(eventData);
                        continue;
                    } catch (Throwable e) {
                        // 出现异常时忽略掉
                        logger.warn("process freedom data error " + eventData.toString(), e);
                        removeDatas.add(eventData);
                        continue;
                    }
                } else {
                    removeDatas.add(eventData);// 删除该记录
                }
            }
        }

        if (!CollectionUtils.isEmpty(removeDatas)) {
            eventDatas.removeAll(removeDatas);
        }
    }

    private EventColumn getMatchColumn(List<EventColumn> columns, String columnName) {
        for (EventColumn column : columns) {
            if (StringUtils.equalsIgnoreCase(column.getColumnName(), columnName)) {
                return column;
            }
        }

        return null;
    }

}
