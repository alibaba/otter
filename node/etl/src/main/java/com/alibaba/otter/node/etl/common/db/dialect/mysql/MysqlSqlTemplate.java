package com.alibaba.otter.node.etl.common.db.dialect.mysql;

import com.alibaba.otter.node.etl.common.db.dialect.AbstractSqlTemplate;

/**
 * mysql sql生成模板
 * 
 * @author jianghang 2011-10-27 下午01:41:20
 * @version 4.0.0
 */
public class MysqlSqlTemplate extends AbstractSqlTemplate {

    private static final String ESCAPE = "`";

    public String getMergeSql(String schemaName, String tableName, String[] pkNames, String[] columnNames,
                              String[] viewColumnNames) {
        StringBuilder sql = new StringBuilder("insert into " + getFullName(schemaName, tableName) + "(");
        int size = columnNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(appendEscape(columnNames[i])).append(" , ");
        }
        size = pkNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(appendEscape(pkNames[i])).append((i + 1 < size) ? " , " : "");
        }

        sql.append(") values (");
        size = columnNames.length;
        for (int i = 0; i < size; i++) {
            sql.append("?").append(" , ");
        }
        size = pkNames.length;
        for (int i = 0; i < size; i++) {
            sql.append("?").append((i + 1 < size) ? " , " : "");
        }
        sql.append(")");
        sql.append(" on duplicate key update ");

        // mysql merge sql匹配了uniqe / primary key时都会执行update，所以需要更新pk信息
        size = pkNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(appendEscape(pkNames[i])).append("=values(").append(appendEscape(pkNames[i])).append(")").append(" , ");
        }

        size = columnNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(appendEscape(columnNames[i])).append("=values(").append(appendEscape(columnNames[i])).append(")");
            sql.append((i + 1 < size) ? " , " : "");
        }

        return sql.toString().intern();// intern优化，避免出现大量相同的字符串
    }

    protected String appendEscape(String columnName) {
        return ESCAPE + columnName + ESCAPE;
    }

}
