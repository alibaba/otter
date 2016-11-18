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

package com.alibaba.otter.node.etl.common.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.jtester.annotations.SpringBeanByName;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.BaseDbTest;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
import com.alibaba.otter.node.etl.common.db.dialect.SqlTemplate;
import com.alibaba.otter.node.etl.common.db.dialect.mysql.MysqlDialect;
import com.alibaba.otter.node.etl.common.db.dialect.oracle.OracleDialect;
import com.alibaba.otter.node.etl.common.db.utils.SqlUtils;
import com.alibaba.otter.shared.common.model.config.data.db.DbDataMedia;

public class DbDialectTest extends BaseDbTest {

    private static final String MYSQL_SCHEMA_NAME  = "srf";
    private static final String ORACLE_SCHEMA_NAME = "srf";
    private static final String TABLE_NAME         = "columns";
    @SpringBeanByName
    private DbDialectFactory    dbDialectFactory;
    // private String[] allColumns = { "alias_name", "amount", "text_b",
    // "text_c", "curr_date",
    // "gmt_create", "gmt_modify", "id", "name" };

    private String[]            pkColumns          = { "id", "name" };
    private String[]            columns            = { "alias_name", "amount", "text_b", "text_c", "curr_date",
            "gmt_create", "gmt_modify"            };

    private String[]            pkColumnValues     = { "1", "ljh" };

    // [116,101,120,116,95,98]
    private String[]            columnValues       = { "hello", "100.01", "text_b", "text_c", "2011-01-01",
            "2011-01-01 11:11:11", "2011-01-01 11:11:11" };

    @Test(expectedExceptions = RuntimeException.class)
    public void test_mysql() {
        DbDataMedia media = getMysqlMedia();
        final DbDialect dbDialect = dbDialectFactory.getDbDialect(2L, media.getSource());
        want.object(dbDialect).clazIs(MysqlDialect.class);

        final SqlTemplate sqlTemplate = dbDialect.getSqlTemplate();
        final JdbcTemplate jdbcTemplate = dbDialect.getJdbcTemplate();
        final TransactionTemplate transactionTemplate = dbDialect.getTransactionTemplate();
        final int[] pkColumnTypes = { Types.INTEGER, Types.VARCHAR };
        final int[] columnTypes = { Types.CHAR, Types.DECIMAL, Types.BLOB, Types.CLOB, Types.DATE, Types.TIMESTAMP,
                Types.TIMESTAMP };
        transactionTemplate.execute(new TransactionCallback() {

            public Object doInTransaction(TransactionStatus status) {
                int affect = 0;
                String sql = null;
                // 执行insert
                sql = sqlTemplate.getInsertSql(MYSQL_SCHEMA_NAME, TABLE_NAME, pkColumns, columns);
                System.out.println(sql);
                affect = (Integer) jdbcTemplate.execute(sql, new PreparedStatementCallback() {

                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        doPreparedStatement(ps,
                            dbDialect,
                            toTypes(columnTypes, pkColumnTypes),
                            toValues(columnValues, pkColumnValues));
                        return ps.executeUpdate();
                    }

                });
                want.number(affect).isEqualTo(1);
                // 执行update
                sql = sqlTemplate.getUpdateSql(MYSQL_SCHEMA_NAME, TABLE_NAME, pkColumns, columns);
                System.out.println(sql);
                affect = (Integer) jdbcTemplate.execute(sql, new PreparedStatementCallback() {

                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        doPreparedStatement(ps,
                            dbDialect,
                            toTypes(columnTypes, pkColumnTypes),
                            toValues(columnValues, pkColumnValues));
                        return ps.executeUpdate();
                    }

                });
                want.number(affect).isEqualTo(1);
                // 执行deleate
                sql = sqlTemplate.getDeleteSql(MYSQL_SCHEMA_NAME, TABLE_NAME, pkColumns);
                System.out.println(sql);
                affect = (Integer) jdbcTemplate.execute(sql, new PreparedStatementCallback() {

                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        doPreparedStatement(ps, dbDialect, toTypes(pkColumnTypes), toValues(pkColumnValues));
                        return ps.executeUpdate();
                    }

                });
                want.number(affect).isEqualTo(1);
                // 执行merge
                sql = sqlTemplate.getMergeSql(MYSQL_SCHEMA_NAME, TABLE_NAME, pkColumns, columns, null, true);
                System.out.println(sql);
                affect = (Integer) jdbcTemplate.execute(sql, new PreparedStatementCallback() {

                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        doPreparedStatement(ps,
                            dbDialect,
                            toTypes(columnTypes, pkColumnTypes),
                            toValues(columnValues, pkColumnValues));
                        return ps.executeUpdate();
                    }

                });
                want.number(affect).isEqualTo(1);
                throw new RuntimeException("rollback");
            }
        });

    }

    @Test(expectedExceptions = RuntimeException.class)
    public void test_oracle() {
        DbDataMedia media = getOracleMedia();
        final DbDialect dbDialect = dbDialectFactory.getDbDialect(1L, media.getSource());

        want.object(dbDialect).clazIs(OracleDialect.class);
        final SqlTemplate sqlTemplate = dbDialect.getSqlTemplate();
        final JdbcTemplate jdbcTemplate = dbDialect.getJdbcTemplate();
        final TransactionTemplate transactionTemplate = dbDialect.getTransactionTemplate();
        final int[] pkColumnTypes = { Types.NUMERIC, Types.VARCHAR };
        final int[] columnTypes = { Types.CHAR, Types.NUMERIC, Types.BLOB, Types.CLOB, Types.DATE, Types.DATE,
                Types.DATE };
        transactionTemplate.execute(new TransactionCallback() {

            public Object doInTransaction(TransactionStatus status) {
                int affect = 0;
                String sql = null;
                // 执行insert
                sql = sqlTemplate.getInsertSql(ORACLE_SCHEMA_NAME, TABLE_NAME, pkColumns, columns);
                System.out.println(sql);
                affect = (Integer) jdbcTemplate.execute(sql, new PreparedStatementCallback() {

                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        doPreparedStatement(ps,
                            dbDialect,
                            toTypes(columnTypes, pkColumnTypes),
                            toValues(columnValues, pkColumnValues));
                        return ps.executeUpdate();
                    }

                });
                want.number(affect).isEqualTo(1);
                // 执行update
                sql = sqlTemplate.getUpdateSql(ORACLE_SCHEMA_NAME, TABLE_NAME, pkColumns, columns);
                System.out.println(sql);
                affect = (Integer) jdbcTemplate.execute(sql, new PreparedStatementCallback() {

                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        doPreparedStatement(ps,
                            dbDialect,
                            toTypes(columnTypes, pkColumnTypes),
                            toValues(columnValues, pkColumnValues));
                        return ps.executeUpdate();
                    }

                });
                want.number(affect).isEqualTo(1);
                // 执行deleate
                sql = sqlTemplate.getDeleteSql(ORACLE_SCHEMA_NAME, TABLE_NAME, pkColumns);
                System.out.println(sql);
                affect = (Integer) jdbcTemplate.execute(sql, new PreparedStatementCallback() {

                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        doPreparedStatement(ps, dbDialect, toTypes(pkColumnTypes), toValues(pkColumnValues));
                        return ps.executeUpdate();
                    }

                });
                want.number(affect).isEqualTo(1);
                // 执行merge
                sql = sqlTemplate.getMergeSql(ORACLE_SCHEMA_NAME, TABLE_NAME, pkColumns, columns, null, true);
                System.out.println(sql);

                affect = (Integer) jdbcTemplate.execute(sql, new PreparedStatementCallback() {

                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        doPreparedStatement(ps,
                            dbDialect,
                            toTypes(columnTypes, pkColumnTypes),
                            toValues(columnValues, pkColumnValues));
                        return ps.executeUpdate();
                    }

                });
                want.number(affect).isEqualTo(1);
                throw new RuntimeException("rollback");
            }
        });
    }

    private Integer[] toTypes(int[]... types) {
        List<Integer> result = new ArrayList<Integer>();
        for (int[] type : types) {
            for (int t : type) {
                result.add(t);
            }
        }

        return result.toArray(new Integer[result.size()]);
    }

    private String[] toValues(String[]... values) {
        List<String> result = new ArrayList<String>();
        for (String[] value : values) {
            for (String v : value) {
                result.add(v);
            }
        }

        return result.toArray(new String[result.size()]);
    }

    private void doPreparedStatement(PreparedStatement ps, final DbDialect dbDialect, final Integer[] columnTypes,
                                     final String[] columnValues) throws SQLException {
        LobCreator lobCreator = null;
        for (int i = 0; i < columnTypes.length; i++) {
            int paramIndex = i + 1;
            String sqlValue = columnValues[i];
            int sqlType = columnTypes[i];
            Object param = SqlUtils.stringToSqlValue(sqlValue,
                sqlType,
                SqlUtils.isTextType(sqlType),
                dbDialect.isEmptyStringNulled());
            switch (sqlType) {
                case Types.CLOB:
                    if (lobCreator == null) {
                        lobCreator = dbDialect.getLobHandler().getLobCreator();
                    }

                    lobCreator.setClobAsString(ps, paramIndex, (String) param);
                    break;

                case Types.BLOB:
                    if (lobCreator == null) {
                        lobCreator = dbDialect.getLobHandler().getLobCreator();
                    }

                    lobCreator.setBlobAsBytes(ps, paramIndex, (byte[]) param);
                    break;

                default:
                    StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, null, param);
                    break;
            }
        }
    }
}
