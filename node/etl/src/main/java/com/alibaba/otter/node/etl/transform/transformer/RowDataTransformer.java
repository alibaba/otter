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

package com.alibaba.otter.node.etl.transform.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
import com.alibaba.otter.node.etl.transform.exception.TransformException;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMedia.ModeValue;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * RowData -> RowData数据的转换
 * 
 * @author jianghang 2011-10-27 下午04:11:45
 * @version 4.0.0
 */
public class RowDataTransformer extends AbstractOtterTransformer<EventData, EventData> {

    private DbDialectFactory dbDialectFactory;

    public EventData transform(EventData data, OtterTransformerContext context) {
        EventData result = new EventData();
        // 处理Table转化
        DataMedia dataMedia = context.getDataMediaPair().getTarget();
        result.setPairId(context.getDataMediaPair().getId());
        result.setTableId(dataMedia.getId());
        // 需要特殊处理下multi场景
        buildName(data, result, context.getDataMediaPair());
        result.setEventType(data.getEventType());
        result.setExecuteTime(data.getExecuteTime());
        result.setSyncConsistency(data.getSyncConsistency());
        result.setRemedy(data.isRemedy());
        result.setSyncMode(data.getSyncMode());
        result.setSize(data.getSize());
        result.setHint(data.getHint());
        result.setWithoutSchema(data.isWithoutSchema());
        if (data.getEventType().isDdl()) {
            // ddl不需要处理字段
            if (StringUtils.equalsIgnoreCase(result.getSchemaName(), data.getSchemaName())
                && StringUtils.equalsIgnoreCase(result.getTableName(), data.getTableName())) {
                // 是否需要对ddl sql进行转化，暂时不支持异构，必须保证源表和目标表的名字相同
                result.setDdlSchemaName(data.getDdlSchemaName());
                result.setSql(data.getSql());
                return result;
            } else {
                throw new TransformException("no support ddl for [" + data.getSchemaName() + "." + data.getTableName()
                                             + "] to [" + result.getSchemaName() + "." + result.getTableName()
                                             + "] , sql :" + data.getSql());
            }
        }

        Multimap<String, String> translateColumnNames = HashMultimap.create();
        if (context.getDataMediaPair().getColumnPairMode().isInclude()) { // 只针对正向匹配进行名字映射，exclude不做处理
            List<ColumnPair> columnPairs = context.getDataMediaPair().getColumnPairs();
            for (ColumnPair columnPair : columnPairs) {
                translateColumnNames.put(columnPair.getSourceColumn().getName(), columnPair.getTargetColumn().getName());
            }
        }
        // 准备一下table meta
        DataMediaPair dataMediaPair = context.getDataMediaPair();
        boolean useTableTransform = context.getPipeline().getParameters().getUseTableTransform();
        boolean enableCompatibleMissColumn = context.getPipeline().getParameters().getEnableCompatibleMissColumn();
        TableInfoHolder tableHolder = null;
        if (useTableTransform || enableCompatibleMissColumn) {// 控制一下是否需要反查table
                                                              // meta信息，如果同构数据库，完全没必要反查
            // 获取目标库的表信息
            DbDialect dbDialect = dbDialectFactory.getDbDialect(dataMediaPair.getPipelineId(),
                (DbMediaSource) dataMedia.getSource());

            Table table = dbDialect.findTable(result.getSchemaName(), result.getTableName());
            tableHolder = new TableInfoHolder(table, useTableTransform, enableCompatibleMissColumn);
        }

        // 处理column转化
        List<EventColumn> otherColumns = translateColumns(result,
            data.getColumns(),
            context.getDataMediaPair(),
            translateColumnNames,
            tableHolder);
        translatePkColumn(result,
            data.getKeys(),
            data.getOldKeys(),
            otherColumns,
            context.getDataMediaPair(),
            translateColumnNames,
            tableHolder);

        result.setColumns(otherColumns);
        return result;
    }

    /**
     * 设置对应的目标库schema.name，需要考虑mutl配置情况
     * 
     * <pre>
     * case:
     * 1. 源:offer , 目：offer
     * 2. 源:offer[1-128] , 目：offer
     * 3. 源:offer[1-128] , 目：offer[1-128]
     * 4. 源:offer , 目：offer[1-128] 不支持，会报错
     */
    private void buildName(EventData data, EventData result, DataMediaPair pair) {
        DataMedia targetDataMedia = pair.getTarget();
        DataMedia sourceDataMedia = pair.getSource();
        String schemaName = buildName(data.getSchemaName(),
            sourceDataMedia.getNamespaceMode(),
            targetDataMedia.getNamespaceMode());
        String tableName = buildName(data.getTableName(), sourceDataMedia.getNameMode(), targetDataMedia.getNameMode());
        result.setSchemaName(schemaName);
        result.setTableName(tableName);
    }

    private String buildName(String name, ModeValue sourceModeValue, ModeValue targetModeValue) {
        if (targetModeValue.getMode().isWildCard()) {
            return name; // 通配符，认为源和目标一定是一致的
        } else if (targetModeValue.getMode().isMulti()) {
            int index = ConfigHelper.indexIgnoreCase(sourceModeValue.getMultiValue(), name);
            if (index == -1) {
                throw new TransformException("can not found namespace or name in media:" + sourceModeValue.toString());
            }

            return targetModeValue.getMultiValue().get(index);
        } else {
            return targetModeValue.getSingleValue();
        }
    }

    // 处理字段映射
    private List<EventColumn> translateColumns(EventData data, List<EventColumn> columns, DataMediaPair dataMediaPair,
                                               Multimap<String, String> translateColumnNames,
                                               TableInfoHolder tableHolder) {
        List<EventColumn> tcolumns = new ArrayList<EventColumn>();
        for (EventColumn scolumn : columns) {
            EventColumn tcolumn = translateColumn(data, scolumn, tableHolder, dataMediaPair, translateColumnNames);
            if (tcolumn != null) {
                tcolumns.add(tcolumn);
            }
        }
        return tcolumns;
    }

    private void translatePkColumn(EventData data, List<EventColumn> pks, List<EventColumn> oldPks,
                                   List<EventColumn> columns, DataMediaPair dataMediaPair,
                                   Multimap<String, String> translateColumnNames, TableInfoHolder tableHolder) {
        if (CollectionUtils.isEmpty(oldPks)) { // 如果不存在主键变更
            List<EventColumn> tpks = new ArrayList<EventColumn>();
            for (EventColumn scolumn : pks) {
                EventColumn tcolumn = translateColumn(data, scolumn, tableHolder, dataMediaPair, translateColumnNames);
                if (tcolumn != null) {
                    tpks.add(tcolumn);
                }
            }

            data.setKeys(tpks);
        } else { // 存在主键变更
            // modify by ljh at 2012-11-07 , 只做view视图映射的转化，不再做update table xxx
            // set pk = newPK where pk = oldPk的处理
            List<EventColumn> tnewPks = new ArrayList<EventColumn>();
            List<EventColumn> toldPks = new ArrayList<EventColumn>();
            for (int i = 0; i < pks.size(); i++) {
                EventColumn newPk = pks.get(i);
                EventColumn oldPk = oldPks.get(i);
                // 转化new pk
                EventColumn tnewPk = translateColumn(data, newPk, tableHolder, dataMediaPair, translateColumnNames);
                if (tnewPk != null) {
                    tnewPks.add(tnewPk);
                    // 转化old pk，这里不能再用translateColumnNames了，因为转化new
                    // pk已经remove过一次view name了
                    toldPks.add(translateColumn(tnewPk, oldPk.getColumnValue(), dataMediaPair));
                }
            }

            data.setKeys(tnewPks);
            data.setOldKeys(toldPks);

            // 主键变更构建的sql规则如下：
            // update table xxx set pk = newPK where pk = oldPk;
            // for (int i = 0; i < pks.size(); i++) {
            // EventColumn scolumn = pks.get(i);
            // EventColumn oldPk = oldPks.get(i);
            //
            // EventColumn updatePk = translateColumn(scolumn, tableHolder,
            // dataMediaPair, translateColumnNames);
            // if (scolumn.getColumnValue().equals(oldPk.getColumnValue())) {//
            // 主键内容没变更
            // tcolumns.add(updatePk);
            // } else {
            // columns.add(updatePk);// 添加到变更字段中, 设置set pk = newPK的内容
            // // 设置where pk = oldPk的条件
            // tcolumns.add(translateColumn(updatePk, oldPk.getColumnValue(),
            // dataMediaPair));
            // }
            // }
        }
    }

    private EventColumn translateColumn(EventData data, EventColumn scolumn, TableInfoHolder tableHolder,
                                        DataMediaPair dataMediaPair, Multimap<String, String> translateColumnNames) {
        EventType type = data.getEventType();
        EventColumn tcolumn = new EventColumn();
        tcolumn.setNull(scolumn.getColumnValue() == null);
        tcolumn.setKey(scolumn.isKey());// 左右两边的主键值必须保持一样，可以不为物理主键
        tcolumn.setIndex(scolumn.getIndex());
        tcolumn.setUpdate(scolumn.isUpdate());

        String columnName = translateColumnName(scolumn.getColumnName(), dataMediaPair, translateColumnNames);
        if (StringUtils.isBlank(columnName)) {
            throw new TransformException("can't translate column name:" + scolumn.getColumnName() + "in pair:"
                                         + dataMediaPair.toString());
        }

        // 特殊处理
        // columnName = StringUtils.remove(columnName, "`"); //
        // 处理下特殊字符，eromanga给了错误的字段名
        tcolumn.setColumnName(columnName);
        tcolumn.setColumnType(scolumn.getColumnType());// 不反查，直接使用源库的类型
        if (tableHolder != null) {
            // modify by ljh at 2013-01-23
            // 双向同步新增字段，在一边加了字段后，虽然新的字段没有产生业务变化，但会因为某些原因导致传递了新的字段到T模块
            // 此时在目标库并不存在这个字段，导致一直挂起。ps. mysql新增字段时间不是一般的长
            // 所以，做了一个容错处理，针对目标库不存在的字段，如果变更记录在源库不存在变更，并且是null值的，允许丢弃该字段(其实最好还是要判断源库的column的defaultValue和当前值是否一致)
            boolean canColumnsNotExist = tableHolder.isEnableCompatibleMissColumn();
            if (type == EventType.UPDATE) {
                // 非变更字段，且当前值为null
                canColumnsNotExist &= !scolumn.isUpdate() && scolumn.isNull();
            } else if (type == EventType.INSERT) {
                // 当前值为null
                canColumnsNotExist &= scolumn.isNull();
            } else if (type == EventType.DELETE) {
                canColumnsNotExist &= !scolumn.isKey(); // 主键不允许不存在
            }

            Column matchDbColumn = getMatchColumn(tableHolder.getTable().getColumns(), tcolumn.getColumnName());
            // 匹配字段为空，可能源库发生过DDL操作，目标库重新载入一下meta信息
            if (matchDbColumn == null) { // 尝试reload一下table meta
                // 获取目标库的表信息
                DbMediaSource dbMediaSource = (DbMediaSource) dataMediaPair.getTarget().getSource();
                DbDialect dbDialect = dbDialectFactory.getDbDialect(dataMediaPair.getPipelineId(), dbMediaSource);
                String schemaName = tableHolder.getTable().getSchema();
                if (StringUtils.isEmpty(schemaName)) {
                    schemaName = tableHolder.getTable().getCatalog();
                }
                Table table = dbDialect.findTable(schemaName, tableHolder.getTable().getName(), false); // 强制反查一次，并放入cache

                tableHolder.setTable(table);
                matchDbColumn = getMatchColumn(tableHolder.getTable().getColumns(), tcolumn.getColumnName());
                if (matchDbColumn == null) {
                    if (canColumnsNotExist) {
                        return null;
                    } else {
                        throw new TransformException(scolumn.getColumnName() + " is not found in " + table.toString()
                                                     + " and source : " + dataMediaPair.getTarget().getNamespace()
                                                     + "." + dataMediaPair.getTarget().getName());
                    }
                }
            }

            if (tableHolder.isUseTableTransform()) {
                int sqlType = matchDbColumn.getTypeCode();
                tcolumn.setColumnType(sqlType);
            }
        }

        // if (dataMediaPair.getTarget().getSource().getType().isOracle()) {
        // // 特殊处理下oracle编码
        // String encodeValue = SqlUtils.encoding(scolumn.getColumnValue(),
        // scolumn.getColumnType(),
        // dataMediaPair.getSource().getSource().getEncode(),
        // dataMediaPair.getTarget().getSource().getEncode());
        // tcolumn.setColumnValue(encodeValue);
        // } else {
        // mysql编码转化已经在驱动层面上完成
        tcolumn.setColumnValue(scolumn.getColumnValue());
        // }
        translateColumnNames.remove(scolumn.getColumnName(), columnName);// 删除映射关系，避免下次重复转换
        return tcolumn;
    }

    // 根据pk的值 + oldPk的value重新构造一个column对象，用于where pk = oldValue
    private EventColumn translateColumn(EventColumn scolumn, String newValue, DataMediaPair dataMediaPair) {
        EventColumn tcolumn = new EventColumn();
        tcolumn.setNull(newValue == null);
        tcolumn.setKey(scolumn.isKey());// 左右两边的主键值必须保持一样，可以不为物理主键
        tcolumn.setIndex(scolumn.getIndex());
        tcolumn.setColumnName(scolumn.getColumnName());
        tcolumn.setColumnType(scolumn.getColumnType());
        tcolumn.setUpdate(scolumn.isUpdate());
        // if (dataMediaPair.getTarget().getSource().getType().isOracle()) {
        // // 特殊处理下oracle编码
        // String encodeValue = SqlUtils.encoding(newValue,
        // scolumn.getColumnType(), dataMediaPair.getSource()
        // .getSource()
        // .getEncode(), dataMediaPair.getTarget().getSource().getEncode());
        // tcolumn.setColumnValue(encodeValue);
        // } else {
        tcolumn.setColumnValue(newValue);
        // }
        return tcolumn;
    }

    // ============ helper method ============

    /**
     * 根据名字在manager配置的映射关系，转化为目标的字段名字
     */
    private String translateColumnName(String srcColumnName, DataMediaPair dataMediaPair,
                                       Multimap<String, String> translateDict) {
        if (dataMediaPair.getColumnPairMode().isExclude() || CollectionUtils.isEmpty(dataMediaPair.getColumnPairs())) {
            return srcColumnName; // 默认同名
        }

        Collection<String> tColumnNames = translateDict.get(srcColumnName);
        if (CollectionUtils.isEmpty(tColumnNames)) {
            throw new TransformException(srcColumnName + " is not found in column pairs: " + translateDict.toString());
        }
        String columnName = tColumnNames.iterator().next();

        return columnName;
    }

    private Column getMatchColumn(Column[] columns, String columnName) {
        // 目标库字段的类型确定
        for (Column column : columns) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }

        return null;
    }

    // =============== setter / getter =============

    public void setDbDialectFactory(DbDialectFactory dbDialectFactory) {
        this.dbDialectFactory = dbDialectFactory;
    }

    /**
     * 实现可reload的table meta，可替换table属性.
     * 
     * @author jianghang 2012-5-16 下午04:34:18
     * @version 4.0.2
     */
    static class TableInfoHolder {

        private Table   table;
        private boolean useTableTransform          = true;
        private boolean enableCompatibleMissColumn = true;

        public TableInfoHolder(Table table, boolean useTableTransform, boolean enableCompatibleMissColumn){
            this.useTableTransform = useTableTransform;
            this.enableCompatibleMissColumn = enableCompatibleMissColumn;
            this.table = table;
        }

        public Table getTable() {
            return table;
        }

        public void setTable(Table table) {
            this.table = table;
        }

        public boolean isUseTableTransform() {
            return useTableTransform;
        }

        public void setUseTableTransform(boolean useTableTransform) {
            this.useTableTransform = useTableTransform;
        }

        public boolean isEnableCompatibleMissColumn() {
            return enableCompatibleMissColumn;
        }

        public void setEnableCompatibleMissColumn(boolean enableCompatibleMissColumn) {
            this.enableCompatibleMissColumn = enableCompatibleMissColumn;
        }

    }
}
