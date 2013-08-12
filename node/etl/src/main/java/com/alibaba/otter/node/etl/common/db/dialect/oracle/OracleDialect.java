package com.alibaba.otter.node.etl.common.db.dialect.oracle;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;

import com.alibaba.otter.node.etl.common.db.dialect.AbstractDbDialect;

/**
 * 基于oracle的一些特殊处理定义
 * 
 * @author jianghang 2011-10-27 下午01:44:46
 * @version 4.0.0
 */
public class OracleDialect extends AbstractDbDialect {

    public OracleDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler){
        super(jdbcTemplate, lobHandler);
        sqlTemplate = new OracleSqlTemplate();
    }

    public OracleDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler, String name, int majorVersion,
                         int minorVersion){
        super(jdbcTemplate, lobHandler, name, majorVersion, minorVersion);
        sqlTemplate = new OracleSqlTemplate();
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
        return true;
    }

    public boolean isSupportMergeSql() {
        return true;
    }

    public String getDefaultCatalog() {
        return null;
    }

    public String getDefaultSchema() {
        return (String) jdbcTemplate.queryForObject("SELECT sys_context('USERENV', 'CURRENT_SCHEMA') FROM dual",
                                                    String.class);
    }

}
