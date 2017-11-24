package com.alibaba.otter.node.etl.common.db.dialect.clickhouse;

import com.alibaba.otter.node.etl.common.db.dialect.AbstractDbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.oracle.OracleSqlTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * Created by liyc on 2017/5/9 0009.
 * 只支持insert，其他的忽略
 */
public class ClickHouseDialect extends AbstractDbDialect {
    public ClickHouseDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler){
        super(jdbcTemplate, lobHandler);
        sqlTemplate = new ClickHouseSqlTemplate();
    }

    public ClickHouseDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler, String name, int majorVersion,
                         int minorVersion){
        super(jdbcTemplate, lobHandler, name, majorVersion, minorVersion);
        sqlTemplate = new ClickHouseSqlTemplate();
    }

    public boolean isCharSpacePadded() {
        return true;
    }

    public boolean isCharSpaceTrimmed() {
        return false;
    }

    public boolean isEmptyStringNulled() {
        return true;
    }

    public boolean storesUpperCaseNamesInCatalog() {
        return false;
    }

    public boolean isSupportMergeSql() {
        return false;
    }

    public String getDefaultCatalog() {
        return "default";
    }

    public String getDefaultSchema() {
        return (String) jdbcTemplate.queryForObject("SELECT currentDatabase()", String.class);
    }

}
