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

package com.alibaba.otter.manager.biz.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.otter.manager.biz.common.DataSourceCreator;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.ModeValueFilter;
import com.alibaba.otter.shared.common.model.config.data.DataMedia.ModeValue;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.utils.meta.DdlSchemaFilter;
import com.alibaba.otter.shared.common.utils.meta.DdlTableNameFilter;
import com.alibaba.otter.shared.common.utils.meta.DdlUtils;

/**
 * @author simon 2011-11-25 下午04:57:55
 */
public class DataSourceChecker {

    private static final Logger    logger             = LoggerFactory.getLogger(DataSourceChecker.class);
    private DataMediaSourceService dataMediaSourceService;
    private DataSourceCreator      dataSourceCreator;

    // private static final String MYSQL_FLAG = "mysql";

    // private static final String ORACLE_FLAG = "oracle";

    // 选择的数据库类型和jdbc-url不匹配
    // private static final String DBTYPE_CONFLICT =
    // "\u9009\u62e9\u7684\u6570\u636e\u5e93\u7c7b\u578b\u548cjdbc-url\u4e0d\u5339\u914d";
    // 恭喜,数据库通过验证!
    private static final String    DATABASE_SUCCESS   = "\u606d\u559c,\u6570\u636e\u5e93\u901a\u8fc7\u9a8c\u8bc1!";
    // 抱歉,数据库未通过验证,请检查相关配置!
    private static final String    DATABASE_FAIL      = "\u62b1\u6b49,\u6570\u636e\u5e93\u672a\u901a\u8fc7\u9a8c\u8bc1,\u8bf7\u68c0\u67e5\u76f8\u5173\u914d\u7f6e!";
    // 恭喜,select操作成功,权限正常!
    private static final String    TABLE_SUCCESS      = "\u606d\u559c,select\u64cd\u4f5c\u6210\u529f,\u6743\u9650\u6b63\u5e38!";
    // 抱歉select操作报错,请检查权限配置!
    private static final String    TABLE_FAIL         = "\u62b1\u6b49,\u64cd\u4f5c\u62a5\u9519,\u8bf7\u68c0\u67e5\u6743\u9650\u914d\u7f6e!";
    // 恭喜,编码验证正确!
    private static final String    ENCODE_QUERY_ERROR = "\u6267\u884cSQL\u51fa\u9519,\u8bf7\u68c0\u67e5\u6570\u636e\u5e93\u7c7b\u578b\u662f\u5426\u9009\u62e9\u6b63\u786e!";
    // 抱歉,字符集不匹配,实际数据库默认字符集为:
    private static final String    ENCODE_FAIL        = "\u62b1\u6b49,\u5b57\u7b26\u96c6\u4e0d\u5339\u914d,\u5b9e\u9645\u6570\u636e\u5e93\u9ed8\u8ba4\u5b57\u7b26\u96c6\u4e3a:";
    // SELECT未成功
    private static final String    SELECT_FAIL        = "SELECT\u672a\u6210\u529f";

    // DELETE未成功
    // private static final String DELETE_FAIL = "DELETE\u672a\u6210\u529f";
    // INSERT未成功
    // private static final String INSERT_FAIL = "INSERT\u672a\u6210\u529f";

    // 分库前缀必须大于后缀，且不能为负数
    // private static final String SPLIT_INDEX_FAIL =
    // "\u5206\u5e93\u524d\u7f00\u5fc5\u987b\u5927\u4e8e\u540e\u7f00\uff0c\u4e14\u4e0d\u80fd\u4e3a\u8d1f\u6570";

    private void closeConnection(Connection conn) {
        closeConnection(conn, null, null);
    }

    private void closeConnection(Connection conn, Statement st) {
        closeConnection(conn, st, null);
    }

    private void closeConnection(Connection conn, Statement st, ResultSet rs) {
        try {
            if (null != rs) {
                rs.close();
            }
            if (null != st) {
                st.close();
            }
            if (null != conn) {
                conn.close();
            }

        } catch (SQLException e) {
            logger.error("", e);
        }
    }

    @SuppressWarnings("resource")
    public String check(String url, String username, String password, String encode, String sourceType) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        // boolean typeConflict = true;
        // if ((sourceType.toLowerCase().equals(MYSQL_FLAG) &&
        // url.toLowerCase().contains(MYSQL_FLAG))
        // || sourceType.toLowerCase().equals(ORACLE_FLAG) &&
        // url.toLowerCase().contains(ORACLE_FLAG)) {
        // typeConflict = false;
        // }
        //
        // if (typeConflict) {
        // return DBTYPE_CONFLICT;
        // }

        DataSource dataSource = null;
        try {

            DbMediaSource dbMediaSource = new DbMediaSource();
            dbMediaSource.setUrl(url);
            dbMediaSource.setUsername(username);
            dbMediaSource.setPassword(password);
            dbMediaSource.setEncode(encode);

            if (sourceType.equalsIgnoreCase("MYSQL")) {
                dbMediaSource.setType(DataMediaType.MYSQL);
                dbMediaSource.setDriver("com.mysql.jdbc.Driver");
            } else if (sourceType.equalsIgnoreCase("ORACLE")) {
                dbMediaSource.setType(DataMediaType.ORACLE);
                dbMediaSource.setDriver("oracle.jdbc.driver.OracleDriver");
            }

            dataSource = dataSourceCreator.createDataSource(dbMediaSource);
            try {
                conn = dataSource.getConnection();
            } catch (Exception e) {
                logger.error("check error!", e);
            }

            if (null == conn) {
                return DATABASE_FAIL;
            }

            stmt = conn.createStatement();
            String sql = null;
            if (sourceType.equals("MYSQL")) {
                sql = "SHOW VARIABLES LIKE 'character_set_database'";
            } else if (sourceType.equals("ORACLE")) {
                // sql
                // ="select * from V$NLS_PARAMETERS where parameter in('NLS_LANGUAGE','NLS_TERRITORY','NLS_CHARACTERSET')";
                sql = "select * from V$NLS_PARAMETERS where parameter in('NLS_CHARACTERSET')";
            }
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String defaultEncode = null;
                if (sourceType.equals("MYSQL")) {
                    defaultEncode = ((String) rs.getObject(2)).toLowerCase();
                    defaultEncode = defaultEncode.equals("iso-8859-1") ? "latin1" : defaultEncode;
                    if (!encode.toLowerCase().equals(defaultEncode)) {
                        return ENCODE_FAIL + defaultEncode;
                    }
                } else if (sourceType.equals("ORACLE")) {
                    // ORACLE查询服务器默认字符集需要管理员权限
                    defaultEncode = ((String) rs.getObject(2)).toLowerCase();
                    defaultEncode = defaultEncode.equalsIgnoreCase("zhs16gbk") ? "gbk" : defaultEncode;
                    defaultEncode = defaultEncode.equalsIgnoreCase("us7ascii") ? "iso-8859-1" : defaultEncode;
                    if (!encode.toLowerCase().equals(defaultEncode)) {
                        return ENCODE_FAIL + defaultEncode;
                    }
                }

            }

        } catch (SQLException se) {
            logger.error("check error!", se);
            return ENCODE_QUERY_ERROR;
        } catch (Exception e) {
            logger.error("check error!", e);
            return DATABASE_FAIL;
        } finally {
            closeConnection(conn);
            dataSourceCreator.destroyDataSource(dataSource);
        }

        return DATABASE_SUCCESS;

    }

    public String checkMap(String namespace, String name, Long dataSourceId) {
        Connection conn = null;
        Statement stmt = null;
        DataMediaSource source = dataMediaSourceService.findById(dataSourceId);
        DataSource dataSource = null;
        try {
            DbMediaSource dbMediaSource = (DbMediaSource) source;
            dataSource = dataSourceCreator.createDataSource(dbMediaSource);
            // conn = dataSource.getConnection();
            // if (null == conn) {
            // return DATABASE_FAIL;
            // }
            ModeValue namespaceValue = ConfigHelper.parseMode(namespace);
            ModeValue nameValue = ConfigHelper.parseMode(name);
            String tempNamespace = namespaceValue.getSingleValue();
            String tempName = nameValue.getSingleValue();

            // String descSql = "desc " + tempNamespace + "." + tempName;
            // stmt = conn.createStatement();

            try {
                Table table = DdlUtils.findTable(new JdbcTemplate(dataSource), tempNamespace, tempNamespace, tempName);
                if (table == null) {
                    return SELECT_FAIL;
                }
            } catch (SQLException se) {
                logger.error("check error!", se);
                return SELECT_FAIL;
            } catch (Exception e) {
                logger.error("check error!", e);
                return SELECT_FAIL;
            }

            // String selectSql = "SELECT * from " + tempNamespace + "." +
            // tempName + " where 1 = 0";
            // String insertSql = "INSERT INTO " + tempNamespace + "." +
            // tempName + " select * from ";
            // insertSql += "( SELECT * from " + tempNamespace + "." + tempName
            // + ") table2 where 1 = 0";
            // String deleteSql = "DELETE from " + tempNamespace + "." +
            // tempName + " where 1 = 0";
            //
            // stmt = conn.createStatement();
            //
            // try {
            // stmt.executeQuery(selectSql);
            // } catch (SQLException se) {
            // return SELECT_FAIL;
            // }
            //
            // try {
            // stmt.execute(insertSql);
            // } catch (SQLException se) {
            // return INSERT_FAIL;
            // }
            //
            // try {
            // stmt.execute(deleteSql);
            // } catch (SQLException se) {
            // return DELETE_FAIL;
            // }

        } finally {
            closeConnection(conn, stmt);
            dataSourceCreator.destroyDataSource(dataSource);
        }

        return TABLE_SUCCESS;

    }

    public String checkNamespaceTables(final String namespace, final String name, final Long dataSourceId) {
        DataSource dataSource = null;
        try {
            DataMediaSource source = dataMediaSourceService.findById(dataSourceId);
            DbMediaSource dbMediaSource = (DbMediaSource) source;
            dataSource = dataSourceCreator.createDataSource(dbMediaSource);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            List<String> schemaList;
            {
                ModeValue mode = ConfigHelper.parseMode(namespace);
                String schemaPattern = ConfigHelper.makeSQLPattern(mode, namespace);
                final ModeValueFilter modeValueFilter = ConfigHelper.makeModeValueFilter(mode, namespace);
                if (source.getType().isOracle()) {
                    schemaList = Arrays.asList(namespace);
                } else {
                    schemaList = DdlUtils.findSchemas(jdbcTemplate, schemaPattern, new DdlSchemaFilter() {

                        @Override
                        public boolean accept(String schemaName) {
                            return modeValueFilter.accept(schemaName);
                        }
                    });
                }
            }

            final List<String> matchSchemaTables = new ArrayList<String>();
            matchSchemaTables.add("Find schema and tables:");
            if (schemaList != null) {
                ModeValue mode = ConfigHelper.parseMode(name);
                String tableNamePattern = ConfigHelper.makeSQLPattern(mode, name);
                final ModeValueFilter modeValueFilter = ConfigHelper.makeModeValueFilter(mode, name);
                for (String schema : schemaList) {
                    DdlUtils.findTables(jdbcTemplate, schema, schema, tableNamePattern, null, new DdlTableNameFilter() {

                        @Override
                        public boolean accept(String catalogName, String schemaName, String tableName) {
                            if (modeValueFilter.accept(tableName)) {
                                matchSchemaTables.add(schemaName + "." + tableName);
                            }
                            return false;
                        }
                    });
                }
            }
            if (matchSchemaTables.size() == 1) {
                return TABLE_FAIL;
            }
            return StringUtils.join(matchSchemaTables, "<br>\n");
        } catch (Exception e) {
            logger.error("check error!", e);
            return TABLE_FAIL;
        } finally {
            dataSourceCreator.destroyDataSource(dataSource);
        }
    }

    public void setDataMediaSourceService(DataMediaSourceService dataMediaSourceService) {
        this.dataMediaSourceService = dataMediaSourceService;
    }

    public void setDataSourceCreator(DataSourceCreator dataSourceCreator) {
        this.dataSourceCreator = dataSourceCreator;
    }

}
