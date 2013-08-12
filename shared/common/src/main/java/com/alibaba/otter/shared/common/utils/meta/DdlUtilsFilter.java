package com.alibaba.otter.shared.common.utils.meta;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author zebin.xuzb @ 2012-8-8
 * @version 4.1.0
 */
public abstract class DdlUtilsFilter {

    /**
     * 返回要获取 {@linkplain DatabaseMetaData} 的 {@linkplain Connection}，不能返回null
     * 
     * @param con
     * @return
     */
    public Connection filterConnection(Connection con) throws Exception {
        return con;
    }

    /**
     * 对 databaseMetaData 做一些过滤,返回 {@linkplain DatabaseMetaData}，不能为 null
     * 
     * @param databaseMetaData
     * @return
     */
    public DatabaseMetaData filterDataBaseMetaData(JdbcTemplate jdbcTemplate, Connection con,
                                                   DatabaseMetaData databaseMetaData) throws Exception {
        return databaseMetaData;
    }

}
