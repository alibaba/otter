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

package com.alibaba.otter.node.etl.load.loader.db.interceptor.operation;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.load.loader.db.context.DbLoadContext;
import com.alibaba.otter.node.etl.load.loader.interceptor.AbstractLoadInterceptor;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.Identity;

/**
 * @author jianghang 2011-10-31 下午02:24:28
 * @version 4.0.0
 */
public abstract class AbstractOperationInterceptor extends AbstractLoadInterceptor<DbLoadContext, EventData> {

    protected final Logger         logger              = LoggerFactory.getLogger(getClass());
    protected static final int     GLOBAL_THREAD_COUNT = 1000;
    protected static final int     INNER_THREAD_COUNT  = 300;
    protected static final String  checkDataSql        = "SELECT COUNT(*) FROM {0} WHERE id BETWEEN 0 AND {1}";
    protected static final String  deleteDataSql       = "DELETE FROM {0}";

    protected String               updateSql;
    protected String               updateInfoSql;
    protected String               clearSql            = "UPDATE {0} SET {1} = 0 WHERE id = ? and {1} = ?";
    protected String               clearInfoSql        = "UPDATE {0} SET {1} = 0 , {2} = null WHERE id = ? and {1} = ? and {2} = ?";
    protected int                  innerIdCount        = INNER_THREAD_COUNT;
    protected int                  globalIdCount       = GLOBAL_THREAD_COUNT;
    protected ConfigClientService  configClientService;
    protected Set<JdbcTemplate>    tableCheckStatus    = Collections.synchronizedSet(new HashSet<JdbcTemplate>());
    protected AtomicInteger        THREAD_COUNTER      = new AtomicInteger(0);
    protected ThreadLocal<Integer> threadLocal         = new ThreadLocal<Integer>();

    protected AbstractOperationInterceptor(String updateSql, String updateInfoSql){
        this.updateSql = updateSql;
        this.updateInfoSql = updateInfoSql;
    }

    private void init(final JdbcTemplate jdbcTemplate, final String markTableName, final String markTableColumn) {
        int count = jdbcTemplate.queryForInt(MessageFormat.format(checkDataSql, markTableName, GLOBAL_THREAD_COUNT - 1));
        if (count != GLOBAL_THREAD_COUNT) {
            if (logger.isInfoEnabled()) {
                logger.info("Interceptor: init " + markTableName + "'s data.");
            }
            TransactionTemplate transactionTemplate = new TransactionTemplate();
            transactionTemplate.setTransactionManager(new DataSourceTransactionManager(jdbcTemplate.getDataSource()));
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);// 注意这里强制使用非事务，保证多线程的可见性
            transactionTemplate.execute(new TransactionCallback() {

                public Object doInTransaction(TransactionStatus status) {
                    jdbcTemplate.execute(MessageFormat.format(deleteDataSql, markTableName));
                    String batchSql = MessageFormat.format(updateSql, new Object[] { markTableName, markTableColumn });
                    jdbcTemplate.batchUpdate(batchSql, new BatchPreparedStatementSetter() {

                        public void setValues(PreparedStatement ps, int idx) throws SQLException {
                            ps.setInt(1, idx);
                            ps.setInt(2, 0);
                            // ps.setNull(3, Types.VARCHAR);
                        }

                        public int getBatchSize() {
                            return GLOBAL_THREAD_COUNT;
                        }
                    });
                    return null;
                }
            });

            if (logger.isInfoEnabled()) {
                logger.info("Interceptor: Init EROSA Client Data: " + updateSql);
            }
        }

    }

    public void transactionBegin(DbLoadContext context, List<EventData> currentDatas, DbDialect dialect) {
        boolean needInfo = StringUtils.isNotEmpty(context.getPipeline().getParameters().getChannelInfo());
        if (context.getChannel().getPipelines().size() > 1 || needInfo) {// 如果是双向同步，需要记录clientId
            String hint = currentDatas.get(0).getHint();
            String sql = needInfo ? updateInfoSql : updateSql;
            threadLocal.remove();// 进入之前先清理
            int threadId = currentId();
            updateMark(context, dialect, threadId, sql, needInfo, hint);
            threadLocal.set(threadId);
        }
    }

    public void transactionEnd(DbLoadContext context, List<EventData> currentDatas, DbDialect dialect) {
        boolean needInfo = StringUtils.isNotEmpty(context.getPipeline().getParameters().getChannelInfo());
        if (context.getChannel().getPipelines().size() > 1 || needInfo) {// 如果是双向同步，需要记录clientId
            String hint = currentDatas.get(0).getHint();
            String sql = needInfo ? clearInfoSql : clearSql;
            Integer threadId = threadLocal.get();
            updateMark(context, dialect, threadId, sql, needInfo, hint);
            threadLocal.remove();
        }
    }

    /**
     * 更新一下事务标记
     */
    private void updateMark(DbLoadContext context, DbDialect dialect, int threadId, String sql, boolean needInfo,
                            String hint) {
        Identity identity = context.getIdentity();
        Channel channel = context.getChannel();
        // 获取dbDialect
        String markTableName = context.getPipeline().getParameters().getSystemSchema() + "."
                               + context.getPipeline().getParameters().getSystemMarkTable();
        String markTableColumn = context.getPipeline().getParameters().getSystemMarkTableColumn();
        synchronized (dialect.getJdbcTemplate()) {
            if (tableCheckStatus.contains(dialect.getJdbcTemplate()) == false) {
                init(dialect.getJdbcTemplate(), markTableName, markTableColumn);
                tableCheckStatus.add(dialect.getJdbcTemplate());
            }
        }

        int affectedCount = 0;
        if (needInfo) {
            String infoColumn = context.getPipeline().getParameters().getSystemMarkTableInfo();
            String info = context.getPipeline().getParameters().getChannelInfo();// 记录一下channelInfo
            String esql = MessageFormat.format(sql, new Object[] { markTableName, markTableColumn, infoColumn });
            if (hint != null) {
                esql = hint + esql;
            }
            affectedCount = dialect.getJdbcTemplate().update(esql, new Object[] { threadId, channel.getId(), info });
        } else {
            String esql = MessageFormat.format(sql, new Object[] { markTableName, markTableColumn });
            if (hint != null) {
                esql = hint + esql;
            }
            affectedCount = dialect.getJdbcTemplate().update(esql, new Object[] { threadId, channel.getId() });
        }

        if (affectedCount <= 0) {
            logger.warn("## update {} failed by [{}]", markTableName, threadId);
        } else {
            if (logger.isInfoEnabled()) {
                logger.debug("Interceptor For [{}]", identity);
            }
        }
    }

    private int currentId() {
        synchronized (this) {
            if (THREAD_COUNTER.get() == INNER_THREAD_COUNT) {
                THREAD_COUNTER.set(0);
            }

            return THREAD_COUNTER.incrementAndGet();
        }
    }

    // ========================= setter / getter ========================

    public void setInnerIdCount(int innerIdCount) {
        this.innerIdCount = innerIdCount;
    }

    public void setGlobalIdCount(int globalIdCount) {
        this.globalIdCount = globalIdCount;
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

}
