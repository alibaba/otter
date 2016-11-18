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

package com.alibaba.otter.shared.common.utils.meta;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.MetaDataColumnDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

/**
 * copy from otter3.0
 * 
 * @author xiaoqing.zhouxq 2012-3-30 上午10:43:04
 * @author zebin.xuzb add filter for data
 */
public class DdlUtils {

    private static final Logger               logger                = LoggerFactory.getLogger(DdlUtils.class);
    private static TableType[]                SUPPORTED_TABLE_TYPES = new TableType[] { TableType.view, TableType.table };
    private final static Map<Integer, String> _defaultSizes         = new HashMap<Integer, String>();
    static {
        _defaultSizes.put(new Integer(1), "254");
        _defaultSizes.put(new Integer(12), "254");
        _defaultSizes.put(new Integer(-1), "254");
        _defaultSizes.put(new Integer(-2), "254");
        _defaultSizes.put(new Integer(-3), "254");
        _defaultSizes.put(new Integer(-4), "254");
        _defaultSizes.put(new Integer(4), "32");
        _defaultSizes.put(new Integer(-5), "64");
        _defaultSizes.put(new Integer(7), "7,0");
        _defaultSizes.put(new Integer(6), "15,0");
        _defaultSizes.put(new Integer(8), "15,0");
        _defaultSizes.put(new Integer(3), "15,15");
        _defaultSizes.put(new Integer(2), "15,15");
    }

    /**
     * !!! Only supports MySQL
     */
    @SuppressWarnings("unchecked")
    public static List<String> findSchemas(JdbcTemplate jdbcTemplate, final String schemaPattern) {
        try {
            if (StringUtils.isEmpty(schemaPattern)) {
                return jdbcTemplate.query("show databases", new SingleColumnRowMapper(String.class));
            }
            return jdbcTemplate.query("show databases like ?",
                new Object[] { schemaPattern },
                new SingleColumnRowMapper(String.class));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ArrayList<String>();
        }
    }

    /**
     * !!! Only supports MySQL
     */
    public static List<String> findSchemas(JdbcTemplate jdbcTemplate, final String schemaPattern,
                                           final DdlSchemaFilter ddlSchemaFilter) {
        List<String> schemas = findSchemas(jdbcTemplate, schemaPattern);
        if (ddlSchemaFilter == null) {
            return schemas;
        }
        List<String> filterSchemas = new ArrayList<String>();
        for (String schema : schemas) {
            if (ddlSchemaFilter.accept(schema)) {
                filterSchemas.add(schema);
            }
        }
        return filterSchemas;
    }

    public static Table findTable(JdbcTemplate jdbcTemplate, final String catalogName, final String schemaName,
                                  final String tableName) throws Exception {
        return findTable(jdbcTemplate, catalogName, schemaName, tableName, null);
    }

    public static Table findTable(final JdbcTemplate jdbcTemplate, final String catalogName, final String schemaName,
                                  final String tableName, final DdlUtilsFilter filter) throws Exception {
        return (Table) jdbcTemplate.execute(new ConnectionCallback() {

            public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                Table table = null;
                DatabaseMetaDataWrapper metaData = new DatabaseMetaDataWrapper();
                boolean isDRDS = false;
                try {
                    if (filter != null) {
                        con = filter.filterConnection(con);
                        Assert.notNull(con);
                    }
                    DatabaseMetaData databaseMetaData = con.getMetaData();
                    if (filter != null) {
                        databaseMetaData = filter.filterDataBaseMetaData(jdbcTemplate, con, databaseMetaData);
                        Assert.notNull(databaseMetaData);
                    }

                    String databaseName = databaseMetaData.getDatabaseProductName();
                    String version = databaseMetaData.getDatabaseProductVersion();
                    if (StringUtils.startsWithIgnoreCase(databaseName, "mysql")
                        && StringUtils.contains(version, "-TDDL-")) {
                        isDRDS = true;
                    }

                    metaData.setMetaData(databaseMetaData);
                    metaData.setTableTypes(TableType.toStrings(SUPPORTED_TABLE_TYPES));
                    metaData.setCatalog(catalogName);
                    metaData.setSchemaPattern(schemaName);

                    String convertTableName = tableName;
                    if (databaseMetaData.storesUpperCaseIdentifiers()) {
                        metaData.setCatalog(catalogName.toUpperCase());
                        metaData.setSchemaPattern(schemaName.toUpperCase());
                        convertTableName = tableName.toUpperCase();
                    }
                    if (databaseMetaData.storesLowerCaseIdentifiers()) {
                        metaData.setCatalog(catalogName.toLowerCase());
                        metaData.setSchemaPattern(schemaName.toLowerCase());
                        convertTableName = tableName.toLowerCase();
                    }

                    ResultSet tableData = null;
                    try {
                        tableData = metaData.getTables(convertTableName);

                        while ((tableData != null) && tableData.next()) {
                            Map<String, Object> values = readColumns(tableData, initColumnsForTable());

                            table = readTable(metaData, values);
                            if (table.getName().equalsIgnoreCase(tableName)) {
                                break;
                            }
                        }
                    } finally {
                        JdbcUtils.closeResultSet(tableData);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                makeAllColumnsPrimaryKeysIfNoPrimaryKeysFound(table);
                if (isDRDS) {
                    makeDRDSShardColumnsAsPrimaryKeys(table, jdbcTemplate, catalogName, schemaName, tableName);
                }
                return table;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static List<Table> findTables(final JdbcTemplate jdbcTemplate, final String catalogName,
                                         final String schemaName, final String tableNamePattern,
                                         final DdlUtilsFilter filter, final DdlTableNameFilter tableNameFilter)
                                                                                                               throws Exception {
        return (List<Table>) jdbcTemplate.execute(new ConnectionCallback() {

            public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                List<Table> tables = new ArrayList<Table>();
                DatabaseMetaDataWrapper metaData = new DatabaseMetaDataWrapper();
                boolean isDRDS = false;
                try {
                    if (filter != null) {
                        con = filter.filterConnection(con);
                        Assert.notNull(con);
                    }
                    DatabaseMetaData databaseMetaData = con.getMetaData();
                    if (filter != null) {
                        databaseMetaData = filter.filterDataBaseMetaData(jdbcTemplate, con, databaseMetaData);
                        Assert.notNull(databaseMetaData);
                    }

                    String databaseName = databaseMetaData.getDatabaseProductName();
                    String version = databaseMetaData.getDatabaseProductVersion();
                    if (StringUtils.startsWithIgnoreCase(databaseName, "mysql")
                        && StringUtils.contains(version, "-TDDL-")) {
                        isDRDS = true;
                    }

                    metaData.setMetaData(databaseMetaData);
                    metaData.setTableTypes(TableType.toStrings(SUPPORTED_TABLE_TYPES));
                    metaData.setCatalog(catalogName);
                    metaData.setSchemaPattern(schemaName);

                    String convertTableName = tableNamePattern;
                    if (databaseMetaData.storesUpperCaseIdentifiers()) {
                        metaData.setCatalog(catalogName.toUpperCase());
                        metaData.setSchemaPattern(schemaName.toUpperCase());
                        convertTableName = tableNamePattern.toUpperCase();
                    }
                    if (databaseMetaData.storesLowerCaseIdentifiers()) {
                        metaData.setCatalog(catalogName.toLowerCase());
                        metaData.setSchemaPattern(schemaName.toLowerCase());
                        convertTableName = tableNamePattern.toLowerCase();
                    }

                    ResultSet tableData = null;
                    try {
                        tableData = metaData.getTables(convertTableName);

                        while ((tableData != null) && tableData.next()) {
                            Map<String, Object> values = readColumns(tableData, initColumnsForTable());

                            Table table = readTable(metaData, values);
                            if ((tableNameFilter == null)
                                || tableNameFilter.accept(catalogName, schemaName, table.getName())) {
                                tables.add(table);
                            }
                        }
                    } finally {
                        JdbcUtils.closeResultSet(tableData);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                for (Table table : tables) {
                    makeAllColumnsPrimaryKeysIfNoPrimaryKeysFound(table);
                    if (isDRDS) {
                        makeDRDSShardColumnsAsPrimaryKeys(table, jdbcTemplate, catalogName, schemaName, table.getName());
                    }
                }

                return tables;
            }
        });
    }

    /**
     * Treat tables with no primary keys as a table with all primary keys.
     */
    private static void makeAllColumnsPrimaryKeysIfNoPrimaryKeysFound(Table table) {
        if ((table != null) && (table.getPrimaryKeyColumns() != null) && (table.getPrimaryKeyColumns().length == 0)) {
            Column[] allCoumns = table.getColumns();

            for (Column column : allCoumns) {
                column.setPrimaryKey(true);
            }
        }
    }

    private static void makeDRDSShardColumnsAsPrimaryKeys(Table table, final JdbcTemplate jdbcTemplate,
                                                          final String catalogName, final String schemaName,
                                                          final String tableName) {
        String shardColumns = getShardKeyByDRDS(jdbcTemplate, catalogName, schemaName, tableName);
        if (StringUtils.isNotEmpty(shardColumns)) {
            String columns[] = StringUtils.split(shardColumns, ',');
            for (String key : columns) {
                Column col = table.findColumn(key, false);
                if (col != null) {
                    col.setPrimaryKey(true);
                } else {
                    throw new NullPointerException(String.format("%s pk %s is null", tableName, key));
                }
            }
        }
    }

    /**
     * 获取DRDS下表的拆分字段, 返回格式为 id,name
     * 
     * @param dataSource
     * @param schemaName
     * @param tableName
     * @return
     */
    public static String getShardKeyByDRDS(final JdbcTemplate jdbcTemplate, final String catalogName,
                                            final String schemaName, final String tableName) {
        try {
            return (String) jdbcTemplate.execute("show partitions from ?", new PreparedStatementCallback() {

                public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                    DatabaseMetaData metaData = ps.getConnection().getMetaData();
                    // String sName = getIdentifierName(schemaName, metaData);
                    String convertTableName = tableName;
                    if (metaData.storesUpperCaseIdentifiers()) {
                        convertTableName = tableName.toUpperCase();
                    }
                    if (metaData.storesLowerCaseIdentifiers()) {
                        convertTableName = tableName.toLowerCase();
                    }
                    String tName = convertTableName;
                    ps.setString(1, tName);
                    ResultSet rs = ps.executeQuery();
                    String log = null;
                    if (rs.next()) {
                        log = rs.getString("KEYS");
                    }

                    rs.close();
                    return log;
                }
            });
        } catch (DataAccessException e) {
            // 兼容下oracle源库和目标库DRDS表名不一致的情况,识别一下表名不存在
            Throwable cause = e.getRootCause();
            if (cause instanceof SQLException) {
                // ER_NO_SUCH_TABLE
                if (((SQLException) cause).getErrorCode() == 1146) {
                    return null;
                }
            }

            throw e;
        }
    }

    private static Table readTable(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
        String tableName = (String) values.get("TABLE_NAME");
        Table table = null;

        if ((tableName != null) && (tableName.length() > 0)) {
            table = new Table();
            table.setName(tableName);
            table.setType((String) values.get("TABLE_TYPE"));
            table.setCatalog((String) values.get("TABLE_CAT"));
            table.setSchema((String) values.get("TABLE_SCHEM"));
            table.setDescription((String) values.get("REMARKS"));
            table.addColumns(readColumns(metaData, tableName));

            Collection<String> primaryKeys = readPrimaryKeyNames(metaData, tableName);

            for (Object key : primaryKeys) {
                Column col = table.findColumn((String) key, true);

                if (col != null) {
                    col.setPrimaryKey(true);
                } else {
                    throw new NullPointerException(String.format("%s pk %s is null - %s %s",
                        tableName,
                        key,
                        ToStringBuilder.reflectionToString(metaData, ToStringStyle.SIMPLE_STYLE),
                        ToStringBuilder.reflectionToString(values, ToStringStyle.SIMPLE_STYLE)));
                }
            }
        }

        return table;
    }

    private static List<MetaDataColumnDescriptor> initColumnsForTable() {
        List<MetaDataColumnDescriptor> result = new ArrayList<MetaDataColumnDescriptor>();

        result.add(new MetaDataColumnDescriptor("TABLE_NAME", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("TABLE_TYPE", Types.VARCHAR, "UNKNOWN"));
        result.add(new MetaDataColumnDescriptor("TABLE_CAT", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("TABLE_SCHEM", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("REMARKS", Types.VARCHAR));

        return result;
    }

    private static List<MetaDataColumnDescriptor> initColumnsForColumn() {
        List<MetaDataColumnDescriptor> result = new ArrayList<MetaDataColumnDescriptor>();

        // As suggested by Alexandre Borgoltz, we're reading the COLUMN_DEF
        // first because Oracle
        // has problems otherwise (it seemingly requires a LONG column to be the
        // first to be read)
        // See also DDLUTILS-29
        result.add(new MetaDataColumnDescriptor("COLUMN_DEF", Types.VARCHAR));

        // we're also reading the table name so that a model reader impl can
        // filter manually
        result.add(new MetaDataColumnDescriptor("TABLE_NAME", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("COLUMN_NAME", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("TYPE_NAME", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("DATA_TYPE", Types.INTEGER, new Integer(java.sql.Types.OTHER)));
        result.add(new MetaDataColumnDescriptor("NUM_PREC_RADIX", Types.INTEGER, new Integer(10)));
        result.add(new MetaDataColumnDescriptor("DECIMAL_DIGITS", Types.INTEGER, new Integer(0)));
        result.add(new MetaDataColumnDescriptor("COLUMN_SIZE", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("IS_NULLABLE", Types.VARCHAR, "YES"));
        result.add(new MetaDataColumnDescriptor("REMARKS", Types.VARCHAR));

        return result;
    }

    private static List<MetaDataColumnDescriptor> initColumnsForPK() {
        List<MetaDataColumnDescriptor> result = new ArrayList<MetaDataColumnDescriptor>();

        result.add(new MetaDataColumnDescriptor("COLUMN_NAME", Types.VARCHAR));

        // we're also reading the table name so that a model reader impl can
        // filter manually
        result.add(new MetaDataColumnDescriptor("TABLE_NAME", Types.VARCHAR));

        // the name of the primary key is currently only interesting to the pk
        // index name resolution
        result.add(new MetaDataColumnDescriptor("PK_NAME", Types.VARCHAR));

        return result;
    }

    private static List<Column> readColumns(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException {
        ResultSet columnData = null;

        try {
            columnData = metaData.getColumns(tableName, null);

            List<Column> columns = new ArrayList<Column>();
            Map<String, Object> values = null;

            for (; columnData.next(); columns.add(readColumn(metaData, values))) {
                Map<String, Object> tmp = readColumns(columnData, initColumnsForColumn());
                if (tableName.equalsIgnoreCase((String) tmp.get("TABLE_NAME"))) {
                    values = tmp;
                } else {
                    break;
                }
            }

            // if (true == columns.isEmpty()) {
            // Connection con = metaData.getMetaData().getConnection();
            // String propIncludeSynonyms = "includeSynonyms";
            //
            // if (PropertyUtils.isWriteable(con, propIncludeSynonyms)) {
            // try {
            // String includeSynonymsMethodName = "setIncludeSynonyms";
            // boolean previousSynonyms = (Boolean)
            // PropertyUtils.getProperty(con, propIncludeSynonyms);
            //
            // if (logger.isInfoEnabled()) {
            // logger.info("ORACLE: switch includeSynonyms to " +
            // previousSynonyms);
            // }
            //
            // try {
            // MethodUtils.invokeMethod(con, includeSynonymsMethodName,
            // !previousSynonyms);
            // columns = readColumns(metaData, tableName);
            //
            // if (false == columns.isEmpty()) {
            // return columns;
            // }
            // } finally {
            // MethodUtils.invokeMethod(con, includeSynonymsMethodName,
            // previousSynonyms);
            // }
            //
            // throw new SQLException("ORACLE: no column found for " +
            // tableName);
            // } catch (IllegalAccessException e) {
            // logger.error(e.getMessage(), e);
            // } catch (InvocationTargetException e) {
            // logger.error(e.getMessage(), e);
            // } catch (NoSuchMethodException e) {
            // logger.error(e.getMessage(), e);
            // }
            // }
            // }

            return columns;
        } finally {
            JdbcUtils.closeResultSet(columnData);
        }
    }

    private static Column readColumn(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
        Column column = new Column();

        column.setName((String) values.get("COLUMN_NAME"));
        column.setDefaultValue((String) values.get("COLUMN_DEF"));
        column.setTypeCode(((Integer) values.get("DATA_TYPE")).intValue());

        String typeName = (String) values.get("TYPE_NAME");
        // column.setType(typeName);

        if ((typeName != null) && typeName.startsWith("TIMESTAMP")) {
            column.setTypeCode(Types.TIMESTAMP);
        }
        // modify 2013-09-25，处理下unsigned
        if ((typeName != null) && StringUtils.containsIgnoreCase(typeName, "UNSIGNED")) {
            // 如果为unsigned，往上调大一个量级，避免数据溢出
            switch (column.getTypeCode()) {
                case Types.TINYINT:
                    column.setTypeCode(Types.SMALLINT);
                    break;
                case Types.SMALLINT:
                    column.setTypeCode(Types.INTEGER);
                    break;
                case Types.INTEGER:
                    column.setTypeCode(Types.BIGINT);
                    break;
                case Types.BIGINT:
                    column.setTypeCode(Types.DECIMAL);
                    break;
                default:
                    break;
            }
        }

        Integer precision = (Integer) values.get("NUM_PREC_RADIX");

        if (precision != null) {
            column.setPrecisionRadix(precision.intValue());
        }

        String size = (String) values.get("COLUMN_SIZE");

        if (size == null) {
            size = (String) _defaultSizes.get(new Integer(column.getTypeCode()));
        }

        // we're setting the size after the precision and radix in case
        // the database prefers to return them in the size value
        column.setSize(size);

        int scale = 0;
        Object dec_digits = values.get("DECIMAL_DIGITS");

        if (dec_digits instanceof String) {
            scale = (dec_digits == null) ? 0 : NumberUtils.toInt(dec_digits.toString());
        } else if (dec_digits instanceof Integer) {
            scale = (dec_digits == null) ? 0 : (Integer) dec_digits;
        }

        if (scale != 0) {
            column.setScale(scale);
        }

        column.setRequired("NO".equalsIgnoreCase(((String) values.get("IS_NULLABLE")).trim()));
        column.setDescription((String) values.get("REMARKS"));
        return column;
    }

    private static Map<String, Object> readColumns(ResultSet resultSet, List<MetaDataColumnDescriptor> columnDescriptors)
                                                                                                                         throws SQLException {
        Map<String, Object> values = new HashMap<String, Object>();
        MetaDataColumnDescriptor descriptor;

        for (Iterator<MetaDataColumnDescriptor> it = columnDescriptors.iterator(); it.hasNext(); values.put(descriptor.getName(),
            descriptor.readColumn(resultSet))) {
            descriptor = (MetaDataColumnDescriptor) it.next();
        }

        return values;
    }

    private static Collection<String> readPrimaryKeyNames(DatabaseMetaDataWrapper metaData, String tableName)
                                                                                                             throws SQLException {
        ResultSet pkData = null;

        try {
            List<String> pks = new ArrayList<String>();
            Map<String, Object> values;

            for (pkData = metaData.getPrimaryKeys(tableName); pkData.next(); pks.add(readPrimaryKeyName(metaData,
                values))) {
                values = readColumns(pkData, initColumnsForPK());
            }

            return pks;
        } finally {
            JdbcUtils.closeResultSet(pkData);
        }
    }

    private static String readPrimaryKeyName(DatabaseMetaDataWrapper metaData, Map<String, Object> values)
                                                                                                          throws SQLException {
        return (String) values.get("COLUMN_NAME");
    }
}
