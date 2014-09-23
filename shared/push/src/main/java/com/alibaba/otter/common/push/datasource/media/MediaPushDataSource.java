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

package com.alibaba.otter.common.push.datasource.media;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.common.push.supplier.DatasourceChangeCallback;
import com.alibaba.otter.common.push.supplier.DatasourceInfo;
import com.alibaba.otter.common.push.supplier.DatasourceSupplier;
import com.alibaba.otter.common.push.supplier.media.MediaDatasourceSupplier;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;

/**
 * media datasource support
 * 
 * @author jianghang 2013-4-18 下午03:45:44
 * @version 4.1.8
 */
public class MediaPushDataSource implements DataSource {

    private static final Logger logger                        = LoggerFactory.getLogger(MediaPushDataSource.class);

    private volatile DataSource delegate;
    private String              dbGroupKey;
    private DatasourceSupplier  dataSourceSupplier;

    private int                 maxWait                       = 60 * 1000;

    private int                 minIdle                       = 0;

    private int                 initialSize                   = 0;

    private int                 maxActive                     = 32;

    private int                 maxIdle                       = 32;

    private int                 numTestsPerEvictionRun        = -1;

    private int                 timeBetweenEvictionRunsMillis = 60 * 1000;

    private int                 removeAbandonedTimeout        = 5 * 60;

    private int                 minEvictableIdleTimeMillis    = 5 * 60 * 1000;

    private String              originalUrl;
    private String              userName;
    private String              password;
    private String              driverClassName;
    private DataMediaType       dataMediaType;
    private String              encoding;

    public MediaPushDataSource(String originalUrl, String userName, String password, String driverClassName,
                               DataMediaType dataMediaType, String encoding){
        this.originalUrl = originalUrl;
        this.userName = userName;
        this.password = password;
        this.driverClassName = driverClassName;
        this.dataMediaType = dataMediaType;
        this.encoding = encoding;
    }

    public synchronized void init() {
        if (!dataMediaType.isMysql()) {
            throw new UnsupportedOperationException("currently only support mysql type");
        }

        if (delegate != null) {
            return;
        }
        if (dataSourceSupplier == null) {
            dataSourceSupplier = MediaDatasourceSupplier.newInstance(dbGroupKey);
            dataSourceSupplier.start();
            dataSourceSupplier.addSwtichCallback(new DatasourceChangeCallback() {

                public void masterChanged(DatasourceInfo newMaster) {
                    String newUrl = buildMysqlUrl(newMaster.getAddress().getAddress().getHostAddress(),
                                                  newMaster.getAddress().getPort());
                    try {
                        ((BasicDataSource) delegate).close();
                        DataSource newDelegate = doCreateDataSource(newUrl);
                        delegate = newDelegate;
                    } catch (SQLException e) {
                        logger.error("switch master error with url : " + originalUrl, e);
                    }

                }
            });
        }

        DatasourceInfo datasourceInfo = dataSourceSupplier.fetchMaster();
        String url = buildMysqlUrl(datasourceInfo.getAddress().getAddress().getHostAddress(),
                                   datasourceInfo.getAddress().getPort());

        delegate = doCreateDataSource(url);
    }

    private String buildMysqlUrl(String hostIp, int port) {
        StringBuilder sb = new StringBuilder("jdbc:mysql://");
        sb.append(hostIp).append(":").append(port);
        return sb.toString();
    }

    protected DataSource doCreateDataSource(String url) {
        BasicDataSource dbcpDs = new BasicDataSource();

        dbcpDs.setInitialSize(initialSize);// 初始化连接池时创建的连接数
        dbcpDs.setMaxActive(maxActive);// 连接池允许的最大并发连接数，值为非正数时表示不限制
        dbcpDs.setMaxIdle(maxIdle);// 连接池中的最大空闲连接数，超过时，多余的空闲连接将会被释放，值为负数时表示不限制
        dbcpDs.setMinIdle(minIdle);// 连接池中的最小空闲连接数，低于此数值时将会创建所欠缺的连接，值为0时表示不创建
        dbcpDs.setMaxWait(maxWait);// 以毫秒表示的当连接池中没有可用连接时等待可用连接返回的时间，超时则抛出异常，值为-1时表示无限等待
        dbcpDs.setRemoveAbandoned(true);// 是否清除已经超过removeAbandonedTimeout设置的无效连接
        dbcpDs.setLogAbandoned(true);// 当清除无效链接时是否在日志中记录清除信息的标志
        dbcpDs.setRemoveAbandonedTimeout(removeAbandonedTimeout); // 以秒表示清除无效链接的时限
        dbcpDs.setNumTestsPerEvictionRun(numTestsPerEvictionRun);// 确保连接池中没有已破损的连接
        dbcpDs.setTestOnBorrow(false);// 指定连接被调用时是否经过校验
        dbcpDs.setTestOnReturn(false);// 指定连接返回到池中时是否经过校验
        dbcpDs.setTestWhileIdle(true);// 指定连接进入空闲状态时是否经过空闲对象驱逐进程的校验
        dbcpDs.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis); // 以毫秒表示空闲对象驱逐进程由运行状态进入休眠状态的时长，值为非正数时表示不运行任何空闲对象驱逐进程
        dbcpDs.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis); // 以毫秒表示连接被空闲对象驱逐进程驱逐前在池中保持空闲状态的最小时间

        // 动态的参数
        dbcpDs.setDriverClassName(driverClassName);
        dbcpDs.setUrl(url);
        dbcpDs.setUsername(userName);
        dbcpDs.setPassword(password);

        if (dataMediaType.isOracle()) {
            dbcpDs.addConnectionProperty("restrictGetTables", "true");
            dbcpDs.setValidationQuery("select 1 from dual");
        } else if (dataMediaType.isMysql()) {
            // open the batch mode for mysql since 5.1.8
            dbcpDs.addConnectionProperty("useServerPrepStmts", "false");
            dbcpDs.addConnectionProperty("rewriteBatchedStatements", "true");
            dbcpDs.addConnectionProperty("zeroDateTimeBehavior", "convertToNull");// 将0000-00-00的时间类型返回null
            dbcpDs.addConnectionProperty("yearIsDateType", "false");// 直接返回字符串，不做year转换date处理
            if (StringUtils.isNotEmpty(encoding)) {
                dbcpDs.addConnectionProperty("characterEncoding", encoding);
            }
            dbcpDs.setValidationQuery("select 1");
        } else {
            logger.error("ERROR ## Unknow database type");
        }

        return dbcpDs;
    }

    public synchronized void destory() throws SQLException {
        if (delegate != null) {
            BasicDataSource basicDataSource = (BasicDataSource) delegate;
            basicDataSource.close();
            delegate = null;
        }
        if (dataSourceSupplier != null) {
            dataSourceSupplier.stop();
            dataSourceSupplier = null;
        }
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return delegate.getConnection(username, password);
    }

    // implemented from JDK7 @see http://docs.oracle.com/javase/7/docs/api/javax/sql/CommonDataSource.html#getParentLogger()
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        try {
            Method getParentLoggerMethod = CommonDataSource.class.getDeclaredMethod("getParentLogger", new Class<?>[0]);
            return (java.util.logging.Logger) getParentLoggerMethod.invoke(delegate, new Object[0]);
        } catch (NoSuchMethodException e) {
            throw new SQLFeatureNotSupportedException(e);
        } catch (InvocationTargetException e2) {
            throw new SQLFeatureNotSupportedException(e2);
        } catch (IllegalArgumentException e2) {
            throw new SQLFeatureNotSupportedException(e2);
        } catch (IllegalAccessException e2) {
            throw new SQLFeatureNotSupportedException(e2);
        }
    }

    // =============== setter & getter ================
    public DataSource getDelegate() {
        return delegate;
    }

    public String getDbGroupKey() {
        return dbGroupKey;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public int getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public int getRemoveAbandonedTimeout() {
        return removeAbandonedTimeout;
    }

    public int getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public DataMediaType getDataMediaType() {
        return dataMediaType;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setDelegate(DataSource delegate) {
        this.delegate = delegate;
    }

    public void setDbGroupKey(String dbGroupKey) {
        this.dbGroupKey = dbGroupKey;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
    }

    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public void setDataMediaType(DataMediaType dataMediaType) {
        this.dataMediaType = dataMediaType;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

}
