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

package com.alibaba.otter.node.etl.load.loader.db.interceptor.sql;

import java.util.List;

import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
import com.alibaba.otter.node.etl.common.db.dialect.SqlTemplate;
import com.alibaba.otter.node.etl.common.db.dialect.oracle.OracleSqlTemplate;
import com.alibaba.otter.node.etl.load.loader.db.context.DbLoadContext;
import com.alibaba.otter.node.etl.load.loader.interceptor.AbstractLoadInterceptor;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;

/**
 * 计算下最新的sql语句
 * 
 * @author jianghang 2011-12-26 下午12:09:20
 * @version 4.0.0
 */
public class SqlBuilderLoadInterceptor extends AbstractLoadInterceptor<DbLoadContext, EventData> {

    private DbDialectFactory dbDialectFactory;

    public boolean before(DbLoadContext context, EventData currentData) {
        // 初步构建sql
        DbDialect dbDialect = dbDialectFactory.getDbDialect(context.getIdentity().getPipelineId(),
            (DbMediaSource) context.getDataMediaSource());
        SqlTemplate sqlTemplate = dbDialect.getSqlTemplate();
        EventType type = currentData.getEventType();
        String sql = null;

        String schemaName = (currentData.isWithoutSchema() ? null : currentData.getSchemaName());
        // 注意insert/update语句对应的字段数序都是将主键排在后面
        if (type.isInsert()) {
            if (CollectionUtils.isEmpty(currentData.getColumns())
                && (dbDialect.isDRDS() || sqlTemplate instanceof OracleSqlTemplate)) { // 如果表为全主键，直接进行insert
                // sql
                sql = sqlTemplate.getInsertSql(schemaName,
                    currentData.getTableName(),
                    buildColumnNames(currentData.getKeys()),
                    buildColumnNames(currentData.getColumns()));
            } else {
                sql = sqlTemplate.getMergeSql(schemaName,
                    currentData.getTableName(),
                    buildColumnNames(currentData.getKeys()),
                    buildColumnNames(currentData.getColumns()),
                    new String[] {},
                    !dbDialect.isDRDS());
            }
        } else if (type.isUpdate()) {
            // String[] keyColumns = buildColumnNames(currentData.getKeys());
            // String[] otherColumns =
            // buildColumnNames(currentData.getUpdatedColumns());
            // boolean existOldKeys = false;
            // for (String key : keyColumns) {
            // // 找一下otherColumns是否有主键，存在就代表有主键变更
            // if (ArrayUtils.contains(otherColumns, key)) {
            // existOldKeys = true;
            // break;
            // }
            // }

            boolean existOldKeys = !CollectionUtils.isEmpty(currentData.getOldKeys());
            boolean rowMode = context.getPipeline().getParameters().getSyncMode().isRow();
            String[] keyColumns = null;
            String[] otherColumns = null;
            if (existOldKeys) {
                // 需要考虑主键变更的场景
                // 构造sql如下：update table xxx set pk = newPK where pk = oldPk
                keyColumns = buildColumnNames(currentData.getOldKeys());
                // 这里需要精确获取变更的主键,因为目标为DRDS时主键会包含拆分键,正常的原主键变更只更新对应的单主键列即可
                if (dbDialect.isDRDS()) {
                    otherColumns = buildColumnNames(currentData.getUpdatedColumns(), currentData.getUpdatedKeys());
                } else {
                    otherColumns = buildColumnNames(currentData.getUpdatedColumns(), currentData.getKeys());
                }
            } else {
                keyColumns = buildColumnNames(currentData.getKeys());
                otherColumns = buildColumnNames(currentData.getUpdatedColumns());
            }

            if (rowMode && !existOldKeys) {// 如果是行记录,并且不存在主键变更，考虑merge sql
                sql = sqlTemplate.getMergeSql(schemaName,
                    currentData.getTableName(),
                    keyColumns,
                    otherColumns,
                    new String[] {},
                    !dbDialect.isDRDS());
            } else {// 否则进行update sql
                sql = sqlTemplate.getUpdateSql(schemaName, currentData.getTableName(), keyColumns, otherColumns);
            }
        } else if (type.isDelete()) {
            sql = sqlTemplate.getDeleteSql(schemaName,
                currentData.getTableName(),
                buildColumnNames(currentData.getKeys()));
        }

        // 处理下hint sql
        if (currentData.getHint() != null) {
            currentData.setSql(currentData.getHint() + sql);
        } else {
            currentData.setSql(sql);
        }
        return false;
    }

    private String[] buildColumnNames(List<EventColumn> columns) {
        String[] result = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            EventColumn column = columns.get(i);
            result[i] = column.getColumnName();
        }
        return result;
    }

    private String[] buildColumnNames(List<EventColumn> columns1, List<EventColumn> columns2) {
        String[] result = new String[columns1.size() + columns2.size()];
        int i = 0;
        for (i = 0; i < columns1.size(); i++) {
            EventColumn column = columns1.get(i);
            result[i] = column.getColumnName();
        }

        for (; i < columns1.size() + columns2.size(); i++) {
            EventColumn column = columns2.get(i - columns1.size());
            result[i] = column.getColumnName();
        }
        return result;
    }

    // =============== setter / getter =============

    public void setDbDialectFactory(DbDialectFactory dbDialectFactory) {
        this.dbDialectFactory = dbDialectFactory;
    }

}
