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

package com.alibaba.otter.manager.biz.common;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.common.push.datasource.DataSourceHanlder;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;

public class DataSourceCreator implements DisposableBean {

    private static final Logger     logger                        = LoggerFactory.getLogger(DataSourceCreator.class);

    private int                     maxWait                       = 60 * 1000;

    private int                     minIdle                       = 0;

    private int                     initialSize                   = 1;

    private int                     maxActive                     = 5;

    private int                     maxIdle                       = 1;

    private int                     numTestsPerEvictionRun        = -1;

    private int                     timeBetweenEvictionRunsMillis = 60 * 1000;

    private int                     removeAbandonedTimeout        = 10 * 60;

    private int                     minEvictableIdleTimeMillis    = 30 * 60 * 1000;

    private List<DataSourceHanlder> dataSourceHandlers;

    /**
     * 直接创建数据源，不做cache
     */
    public DataSource createDataSource(DataMediaSource dataMediaSource) {
        Assert.notNull(dataMediaSource);
        DbMediaSource dbMediaSource = (DbMediaSource) dataMediaSource;

        // 扩展功能,可以自定义一些自己实现的 dataSource
        DataSource customDataSource = preCreate(0L, dbMediaSource);
        if (customDataSource != null) {
            return customDataSource;
        }

        return createDataSource(dbMediaSource.getUrl(),
            dbMediaSource.getUsername(),
            dbMediaSource.getPassword(),
            dbMediaSource.getDriver(),
            dbMediaSource.getType(),
            dbMediaSource.getEncode());
    }

    public void destroyDataSource(DataSource dataSource) {
        try {
            // for filter to destroy custom datasource
            if (letHandlerDestroyIfSupport(0L, dataSource)) {
                return;
            }

            if (dataSource == null) {
                return;
            }

            BasicDataSource basicDataSource = (BasicDataSource) dataSource;
            basicDataSource.close();
        } catch (SQLException e) {
            logger.error("ERROR ## close the datasource has an error", e);
        }
    }

    public void destroy() throws Exception {
    }

    /**
     * 扩展功能,可以自定义一些自己实现的 dataSource
     */
    private DataSource preCreate(Long pipelineId, DbMediaSource dbMediaSource) {

        if (CollectionUtils.isEmpty(dataSourceHandlers)) {
            return null;
        }

        DataSource dataSource = null;
        for (DataSourceHanlder handler : dataSourceHandlers) {
            if (handler.support(dbMediaSource)) {
                dataSource = handler.create(pipelineId, dbMediaSource);
                if (dataSource != null) {
                    return dataSource;
                }
            }
        }
        return null;
    }

    public boolean letHandlerDestroyIfSupport(Long pipelineId, DataSource source) {
        boolean destroied = false;

        if (CollectionUtils.isEmpty(this.dataSourceHandlers)) {
            return destroied;
        }

        for (DataSourceHanlder handler : this.dataSourceHandlers) {
            if (handler.support(source)) {
                handler.destory(pipelineId);
                destroied = true;
                return destroied;
            }
        }
        return destroied;

    }

    private DataSource createDataSource(String url, String userName, String password, String driverClassName,
                                        DataMediaType dataMediaType, String encoding) {
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
            // dbcpDs.setValidationQuery("select 1 from dual");
        } else if (dataMediaType.isMysql()) {
            // open the batch mode for mysql since 5.1.8
            dbcpDs.addConnectionProperty("useServerPrepStmts", "false");
            dbcpDs.addConnectionProperty("rewriteBatchedStatements", "true");
            dbcpDs.addConnectionProperty("zeroDateTimeBehavior", "convertToNull");// 将0000-00-00的时间类型返回null
            dbcpDs.addConnectionProperty("yearIsDateType", "false");// 直接返回字符串，不做year转换date处理
            dbcpDs.addConnectionProperty("noDatetimeStringSync", "true");// 返回时间类型的字符串,不做时区处理
            if (StringUtils.isNotEmpty(encoding)) {
                if (StringUtils.equalsIgnoreCase(encoding, "utf8mb4")) {
                    dbcpDs.addConnectionProperty("characterEncoding", "utf8");
                    dbcpDs.setConnectionInitSqls(Arrays.asList("set names utf8mb4"));
                } else {
                    dbcpDs.addConnectionProperty("characterEncoding", encoding);
                }
            }
            // dbcpDs.setValidationQuery("select 1");
        } else {
            logger.error("ERROR ## Unknow database type");
        }

        return dbcpDs;
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

    public void setDataSourceHandlers(List<DataSourceHanlder> dataSourceHandlers) {
        this.dataSourceHandlers = dataSourceHandlers;
    }

}
