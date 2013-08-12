package com.alibaba.otter.node.etl.common.db.dialect;

/**
 * sql构造模板操作
 * 
 * @author jianghang 2011-10-27 下午01:31:15
 * @version 4.0.0
 */
public interface SqlTemplate {

    public String getSelectSql(String schemaName, String tableName, String[] pkNames, String[] columnNames);

    public String getUpdateSql(String schemaName, String tableName, String[] pkNames, String[] columnNames);

    public String getDeleteSql(String schemaName, String tableName, String[] pkNames);

    public String getInsertSql(String schemaName, String tableName, String[] pkNames, String[] columnNames);

    /**
     * 获取对应的mergeSql
     */
    public String getMergeSql(String schemaName, String tableName, String[] pkNames, String[] columnNames,
                              String[] viewColumnNames);
}
