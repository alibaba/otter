package com.alibaba.otter.node.etl.common.db.lob;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;

import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;

/**
 * copy from otter3.0，根据不同的数据源自动选择对应的NativeJdbcExtractor
 * 
 * @author jianghang 2011-10-27 下午03:35:17
 * @version 4.0.0
 */
public class AutomaticJdbcExtractor implements NativeJdbcExtractor {

    private NativeJdbcExtractor              defaultJdbcExtractor;
    private Map<String, NativeJdbcExtractor> extractors;
    private NativeJdbcExtractor              jdbcExtractor;

    public AutomaticJdbcExtractor(){
    }

    public boolean isNativeConnectionNecessaryForNativeStatements() {
        return true;
    }

    public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
        return true;
    }

    public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
        return true;
    }

    public Connection getNativeConnection(Connection con) throws SQLException {
        return getJdbcExtractor(con).getNativeConnection(con);
    }

    private synchronized NativeJdbcExtractor getJdbcExtractor(Object o) {
        if (jdbcExtractor == null) {
            String objClass = o.getClass().getName();
            Iterator<String> iterator = extractors.keySet().iterator();

            while (iterator.hasNext()) {
                String classPrefix = iterator.next();

                if (objClass.indexOf(classPrefix) != -1) {
                    jdbcExtractor = (NativeJdbcExtractor) extractors.get(classPrefix);

                    break;
                }
            }

            if (jdbcExtractor == null) {
                jdbcExtractor = defaultJdbcExtractor;
            }
        }

        return jdbcExtractor;
    }

    public Connection getNativeConnectionFromStatement(Statement stmt) throws SQLException {
        return getJdbcExtractor(stmt).getNativeConnectionFromStatement(stmt);
    }

    public Statement getNativeStatement(Statement stmt) throws SQLException {
        return getJdbcExtractor(stmt).getNativeStatement(stmt);
    }

    public PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException {
        return getJdbcExtractor(ps).getNativePreparedStatement(ps);
    }

    public CallableStatement getNativeCallableStatement(CallableStatement cs) throws SQLException {
        return getJdbcExtractor(cs).getNativeCallableStatement(cs);
    }

    public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
        return getJdbcExtractor(rs).getNativeResultSet(rs);
    }

    public Map<String, NativeJdbcExtractor> getExtractors() {
        return extractors;
    }

    public void setExtractors(Map<String, NativeJdbcExtractor> extractors) {
        this.extractors = extractors;
    }

    public NativeJdbcExtractor getDefaultJdbcExtractor() {
        return defaultJdbcExtractor;
    }

    public void setDefaultJdbcExtractor(NativeJdbcExtractor defaultJdbcExtractor) {
        this.defaultJdbcExtractor = defaultJdbcExtractor;
    }
}
