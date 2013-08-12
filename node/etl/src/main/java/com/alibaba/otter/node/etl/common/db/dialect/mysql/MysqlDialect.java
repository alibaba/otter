package com.alibaba.otter.node.etl.common.db.dialect.mysql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;

import com.alibaba.otter.node.etl.common.db.dialect.AbstractDbDialect;

/**
 * 基于mysql的一些特殊处理定义
 * 
 * @author jianghang 2011-10-27 下午01:46:57
 * @version 4.0.0
 */
public class MysqlDialect extends AbstractDbDialect {

    public MysqlDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler){
        super(jdbcTemplate, lobHandler);
        sqlTemplate = new MysqlSqlTemplate();
    }

    public MysqlDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler, String name, int majorVersion,
                        int minorVersion){
        super(jdbcTemplate, lobHandler, name, majorVersion, minorVersion);
        sqlTemplate = new MysqlSqlTemplate();
    }

    public boolean isCharSpacePadded() {
        return false;
    }

    public boolean isCharSpaceTrimmed() {
        return true;
    }

    public boolean isEmptyStringNulled() {
        return false;
    }

    public boolean isSupportMergeSql() {
        return true;
    }

    public String getDefaultSchema() {
        return null;
    }

    public String getDefaultCatalog() {
        return (String) jdbcTemplate.queryForObject("select database()", String.class);
    }

}
