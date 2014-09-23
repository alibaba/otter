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

package com.alibaba.otter.node.etl.common.db.lob;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;

/**
 * A class to lazily instantiate a native JDBC extractor.
 * <p />
 * We need to lazily instantiate it because otherwise Spring will construct it for us, and users might get class not
 * found errors (eg if they're not using Weblogic and Spring tries to load the WeblogicNativeJdbcExtractor, things get
 * ugly).
 */
public class LazyNativeJdbcExtractor implements NativeJdbcExtractor {

    private NativeJdbcExtractor delegatedExtractor;
    private Class               extractorClass;

    public LazyNativeJdbcExtractor(){
    }

    public void setExtractorClass(Class extractorClass) {
        this.extractorClass = extractorClass;
    }

    private synchronized NativeJdbcExtractor getDelegatedExtractor() {
        try {
            if (delegatedExtractor == null) {
                delegatedExtractor = (NativeJdbcExtractor) extractorClass.newInstance();
            }
        } catch (IllegalAccessException e) {
            throw new NestableRuntimeException("Error occurred trying to instantiate a native extractor of type: "
                                               + extractorClass, e);
        } catch (InstantiationException e) {
            throw new NestableRuntimeException("Error occurred trying to instantiate a native extractor of type: "
                                               + extractorClass, e);
        }

        if (delegatedExtractor != null) {
            return delegatedExtractor;
        } else {
            throw new NestableRuntimeException("Error occurred trying to instantiate a native extractor of type: "
                                               + extractorClass);
        }
    }

    public boolean isNativeConnectionNecessaryForNativeStatements() {
        return getDelegatedExtractor().isNativeConnectionNecessaryForNativeStatements();
    }

    public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
        return getDelegatedExtractor().isNativeConnectionNecessaryForNativePreparedStatements();
    }

    public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
        return getDelegatedExtractor().isNativeConnectionNecessaryForNativeCallableStatements();
    }

    public Connection getNativeConnection(Connection con) throws SQLException {
        return getDelegatedExtractor().getNativeConnection(con);
    }

    public Connection getNativeConnectionFromStatement(Statement stmt) throws SQLException {
        return getDelegatedExtractor().getNativeConnectionFromStatement(stmt);
    }

    public Statement getNativeStatement(Statement stmt) throws SQLException {
        return getDelegatedExtractor().getNativeStatement(stmt);
    }

    public PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException {
        return getDelegatedExtractor().getNativePreparedStatement(ps);
    }

    public CallableStatement getNativeCallableStatement(CallableStatement cs) throws SQLException {
        return getDelegatedExtractor().getNativeCallableStatement(cs);
    }

    public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
        return getDelegatedExtractor().getNativeResultSet(rs);
    }
}
