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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.etl.extract.exceptions.ExtractException;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncConsistency;
import com.alibaba.otter.shared.common.model.config.data.ColumnGroup;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;

/**
 * 变更的主键+变更的字段 会和group进行交集处理，发现有交集后会确保当前group的所有字段都可以得到同步
 * 
 * @author simon 2012-4-26 下午5:04:51
 */
public class GroupExtractor extends AbstractExtractor<DbBatch> {

    @Override
    public void extract(DbBatch dbBatch) throws ExtractException {
        Assert.notNull(dbBatch);
        Assert.notNull(dbBatch.getRowBatch());

        Pipeline pipeline = getPipeline(dbBatch.getRowBatch().getIdentity().getPipelineId());
        List<DataMediaPair> dataMediaPairs = pipeline.getPairs();

        /**
         * Key = TableId<br>
         * Value = a List of this tableId's column need to sync<br>
         */
        Map<Long, List<ColumnGroup>> groupColumns = new HashMap<Long, List<ColumnGroup>>();

        for (DataMediaPair dataMediaPair : dataMediaPairs) {
            List<ColumnGroup> columnGroups = dataMediaPair.getColumnGroups();
            if (!CollectionUtils.isEmpty(columnGroups)) {
                groupColumns.put(dataMediaPair.getSource().getId(), columnGroups);
            }
        }

        List<EventData> eventDatas = dbBatch.getRowBatch().getDatas();
        for (EventData eventData : eventDatas) {
            if (eventData.getEventType().isDdl()) {
                continue;
            }

            List<ColumnGroup> columnGroups = groupColumns.get(eventData.getTableId());
            if (!CollectionUtils.isEmpty(columnGroups)) {
                for (ColumnGroup columnGroup : columnGroups) {
                    if (columnGroup != null && !CollectionUtils.isEmpty(columnGroup.getColumnPairs())) {
                        groupFilter(eventData, columnGroup);
                    }
                }
            }
        }
    }

    private void groupFilter(EventData eventData, ColumnGroup columnGroup) {
        List<EventColumn> addColumns = new ArrayList<EventColumn>();

        // 判断一下是否存在字段组内字段的变更
        Set<String> updatedColumns = new HashSet<String>();
        Set<String> pks = new HashSet<String>();

        // 注意，这里只拿实际需要同步变更的字段
        for (EventColumn column : eventData.getUpdatedColumns()) {
            updatedColumns.add(column.getColumnName());
        }
        for (EventColumn pk : eventData.getKeys()) {
            pks.add(pk.getColumnName());
        }

        if (!CollectionUtils.isEmpty(eventData.getOldKeys())) {// 处理变更的主键
            int i = 0;
            for (EventColumn pk : eventData.getKeys()) {
                if (!StringUtils.equals(pk.getColumnValue(), eventData.getOldKeys().get(i).getColumnValue())) {
                    updatedColumns.add(pk.getColumnName());
                }
                i++;
            }
        }

        if (containsInGroupColumn(updatedColumns, columnGroup.getColumnPairs())) {// 存在交集
            // 将变更的字段+变更的主键 去和 group字段进行交集处理
            for (ColumnPair columnPair : columnGroup.getColumnPairs()) {
                boolean groupColumnHasInChangedColunms = false;// 原谅我起这么长的变量名…

                // add by ljh at 2012-11-04
                // 做一个优化：
                // 1. 在select模块如果发现存在FileResolver，会补充完整行记录过来
                // 2. 在group判断update=true字段和Group的定义存在交集时，可以直接使用before记录进行处理，可以减少反查数据库的操作

                // 这里直接拿所有columns，而不是拿实际变更过的updateColumns
                // for (String columnName : updatedColumns) {
                for (EventColumn column : eventData.getColumns()) {
                    if (StringUtils.equalsIgnoreCase(columnPair.getSourceColumn().getName(), column.getColumnName())) {
                        groupColumnHasInChangedColunms = true;
                        if (!column.isUpdate()) {// 如果为非同步字段，强制修改为update=true进行数据同步
                            column.setUpdate(true);
                        }
                        break;
                    }
                }

                if (!groupColumnHasInChangedColunms) {// 不存在对应的变更字段记录
                    String columnName = columnPair.getSourceColumn().getName();
                    if (!pks.contains(columnName)) { // 只添加非主键的值到反查column，因为主键不需要反查
                        EventColumn addColumn = new EventColumn();
                        addColumn.setColumnName(columnPair.getSourceColumn().getName());
                        addColumn.setUpdate(true);
                        addColumns.add(addColumn);
                    }
                }
            }

            if (!CollectionUtils.isEmpty(addColumns)) {
                // 字段去重
                eventData.getColumns().addAll(addColumns);// 添加不足的字段
                eventData.setSyncConsistency(SyncConsistency.MEDIA);
                return;
            }
        }
    }

    /**
     * 检查一下是否出现了字段组中定义的字段
     */
    private boolean containsInGroupColumn(Set<String> columns, List<ColumnPair> columnPairs) {
        for (ColumnPair columnPair : columnPairs) {
            for (String columnName : columns) {
                if (StringUtils.equalsIgnoreCase(columnPair.getSourceColumn().getName(), columnName)) {
                    return true;
                }
            }
        }

        return false;
    }
}
