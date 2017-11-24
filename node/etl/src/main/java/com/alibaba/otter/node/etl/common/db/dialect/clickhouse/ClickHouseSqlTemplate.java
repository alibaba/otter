package com.alibaba.otter.node.etl.common.db.dialect.clickhouse;

import com.alibaba.otter.node.etl.common.db.dialect.AbstractSqlTemplate;

/**
 * Created by liyc on 2017/5/9 0009.
 */
public class ClickHouseSqlTemplate extends AbstractSqlTemplate {

    private static final String ESCAPE = "`";

    public String getMergeSql(String schemaName, String tableName, String[] pkNames, String[] columnNames,
                              String[] viewColumnNames, boolean includePks) {
        return getInsertSql(schemaName,tableName,pkNames,columnNames);
    }

    public String getDeleteSql(String schemaName, String tableName, String[] pkNames) {
        return null;
    }

    public String getUpdateSql(String schemaName, String tableName, String[] pkNames, String[] columnNames) {
        return  null;
    }

    protected String appendEscape(String columnName) {
        return ESCAPE + columnName + ESCAPE;
    }
}
