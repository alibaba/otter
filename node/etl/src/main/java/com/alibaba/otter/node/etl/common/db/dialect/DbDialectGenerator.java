package com.alibaba.otter.node.etl.common.db.dialect;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;

import com.alibaba.otter.node.etl.common.db.dialect.mysql.MysqlDialect;
import com.alibaba.otter.node.etl.common.db.dialect.oracle.OracleDialect;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;

/**
 * @author zebin.xuzb @ 2012-8-8
 * @version 4.1.0
 */
public class DbDialectGenerator {

    protected static final String ORACLE      = "oracle";
    protected static final String MYSQL       = "mysql";
    protected static final String TDDL_GROUP  = "TGroupDatabase";
    protected static final String TDDL_CLIENT = "TDDL";

    protected LobHandler          defaultLobHandler;
    protected LobHandler          oracleLobHandler;

    protected DbDialect generate(JdbcTemplate jdbcTemplate, String databaseName, int databaseMajorVersion,
                                 int databaseMinorVersion, DataMediaType dataMediaType) {
        DbDialect dialect = null;

        if (StringUtils.startsWithIgnoreCase(databaseName, ORACLE)) { // for
                                                                      // oracle
            dialect = new OracleDialect(jdbcTemplate,
                oracleLobHandler,
                databaseName,
                databaseMajorVersion,
                databaseMinorVersion);
        } else if (StringUtils.startsWithIgnoreCase(databaseName, MYSQL)) { // for
                                                                            // mysql
            dialect = new MysqlDialect(jdbcTemplate,
                defaultLobHandler,
                databaseName,
                databaseMajorVersion,
                databaseMinorVersion);
        } else if (StringUtils.startsWithIgnoreCase(databaseName, TDDL_GROUP)) { // for
                                                                                 // tddl
                                                                                 // group
            throw new RuntimeException(databaseName + " type is not support!");
        } else if (StringUtils.startsWithIgnoreCase(databaseName, TDDL_CLIENT)) {
            throw new RuntimeException(databaseName + " type is not support!");
        }

        // diamond is delegated to mysql/oracle, so don't need to extend here

        return dialect;
    }

    // ======== setter =========
    public void setDefaultLobHandler(LobHandler defaultLobHandler) {
        this.defaultLobHandler = defaultLobHandler;
    }

    public void setOracleLobHandler(LobHandler oracleLobHandler) {
        this.oracleLobHandler = oracleLobHandler;
    }
}
