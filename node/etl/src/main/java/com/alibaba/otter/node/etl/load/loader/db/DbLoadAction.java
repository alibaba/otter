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

package com.alibaba.otter.node.etl.load.loader.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
import com.alibaba.otter.node.etl.common.db.dialect.mysql.MysqlDialect;
import com.alibaba.otter.node.etl.common.db.utils.SqlUtils;
import com.alibaba.otter.node.etl.load.exception.LoadException;
import com.alibaba.otter.node.etl.load.loader.LoadStatsTracker;
import com.alibaba.otter.node.etl.load.loader.LoadStatsTracker.LoadCounter;
import com.alibaba.otter.node.etl.load.loader.LoadStatsTracker.LoadThroughput;
import com.alibaba.otter.node.etl.load.loader.db.DbLoadData.TableLoadData;
import com.alibaba.otter.node.etl.load.loader.db.context.DbLoadContext;
import com.alibaba.otter.node.etl.load.loader.interceptor.LoadInterceptor;
import com.alibaba.otter.node.etl.load.loader.weight.WeightBuckets;
import com.alibaba.otter.node.etl.load.loader.weight.WeightController;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

/**
 * 数据库load的执行入口
 * 
 * @author jianghang 2011-10-31 下午03:17:43
 * @version 4.0.0
 */
public class DbLoadAction implements InitializingBean, DisposableBean {

    private static final Logger logger             = LoggerFactory.getLogger(DbLoadAction.class);
    private static final String WORKER_NAME        = "DbLoadAction";
    private static final String WORKER_NAME_FORMAT = "pipelineId = %s , pipelineName = %s , " + WORKER_NAME;
    private static final int    DEFAULT_POOL_SIZE  = 5;
    private int                 poolSize           = DEFAULT_POOL_SIZE;
    private int                 retry              = 3;
    private int                 retryWait          = 3000;
    private LoadInterceptor     interceptor;
    private ExecutorService     executor;
    private DbDialectFactory    dbDialectFactory;
    private ConfigClientService configClientService;
    private int                 batchSize          = 50;
    private boolean             useBatch           = true;
    private LoadStatsTracker    loadStatsTracker;

    /**
     * 返回结果为已处理成功的记录
     */
    public DbLoadContext load(RowBatch rowBatch, WeightController controller) {
        Assert.notNull(rowBatch);
        Identity identity = rowBatch.getIdentity();
        DbLoadContext context = buildContext(identity);

        try {
            List<EventData> datas = rowBatch.getDatas();
            context.setPrepareDatas(datas);
            // 执行重复录入数据过滤
            datas = context.getPrepareDatas();
            if (datas == null || datas.size() == 0) {
                logger.info("##no eventdata for load, return");
                return context;
            }

            // 因为所有的数据在DbBatchLoader已按照DateMediaSource进行归好类，不同数据源介质会有不同的DbLoadAction进行处理
            // 设置media source时，只需要取第一节点的source即可
            context.setDataMediaSource(ConfigHelper.findDataMedia(context.getPipeline(), datas.get(0).getTableId())
                .getSource());
            interceptor.prepare(context);
            // 执行重复录入数据过滤
            datas = context.getPrepareDatas();
            // 处理下ddl语句，ddl/dml语句不可能是在同一个batch中，由canal进行控制
            // 主要考虑ddl的幂等性问题，尽可能一个ddl一个batch，失败或者回滚都只针对这条sql
            if (isDdlDatas(datas)) {
                doDdl(context, datas);
            } else {
                WeightBuckets<EventData> buckets = buildWeightBuckets(context, datas);
                List<Long> weights = buckets.weights();
                controller.start(weights);// weights可能为空，也得调用start方法
                if (CollectionUtils.isEmpty(datas)) {
                    logger.info("##no eventdata for load");
                }
                adjustPoolSize(context); // 根据manager配置调整线程池
                adjustConfig(context); // 调整一下运行参数
                // 按权重构建数据对象
                // 处理数据
                for (int i = 0; i < weights.size(); i++) {
                    Long weight = weights.get(i);
                    controller.await(weight.intValue());
                    // 处理同一个weight下的数据
                    List<EventData> items = buckets.getItems(weight);
                    logger.debug("##start load for weight:" + weight);
                    // 预处理下数据

                    // 进行一次数据合并，合并相同pk的多次I/U/D操作
                    items = DbLoadMerger.merge(items);
                    // 按I/U/D进行归并处理
                    DbLoadData loadData = new DbLoadData();
                    doBefore(items, context, loadData);
                    // 执行load操作
                    doLoad(context, loadData);
                    controller.single(weight.intValue());
                    logger.debug("##end load for weight:" + weight);
                }
            }
            interceptor.commit(context);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            interceptor.error(context);
        } catch (Exception e) {
            interceptor.error(context);
            throw new LoadException(e);
        }

        return context;// 返回处理成功的记录
    }

    private DbLoadContext buildContext(Identity identity) {
        DbLoadContext context = new DbLoadContext();
        context.setIdentity(identity);
        Channel channel = configClientService.findChannel(identity.getChannelId());
        Pipeline pipeline = configClientService.findPipeline(identity.getPipelineId());
        context.setChannel(channel);
        context.setPipeline(pipeline);
        return context;
    }

    /**
     * 分析整个数据，将datas划分为多个批次. ddl sql前的DML并发执行，然后串行执行ddl后，再并发执行DML
     * 
     * @return
     */
    private boolean isDdlDatas(List<EventData> eventDatas) {
        boolean result = false;
        for (EventData eventData : eventDatas) {
            result |= eventData.getEventType().isDdl();
            if (result && !eventData.getEventType().isDdl()) {
                throw new LoadException("ddl/dml can't be in one batch, it's may be a bug , pls submit issues.",
                    DbLoadDumper.dumpEventDatas(eventDatas));
            }
        }

        return result;
    }

    /**
     * 构建基于weight权重分组的item集合列表
     */
    private WeightBuckets<EventData> buildWeightBuckets(DbLoadContext context, List<EventData> datas) {
        WeightBuckets<EventData> buckets = new WeightBuckets<EventData>();
        for (EventData data : datas) {
            // 获取对应的weight
            DataMediaPair pair = ConfigHelper.findDataMediaPair(context.getPipeline(), data.getPairId());
            buckets.addItem(pair.getPushWeight(), data);
        }

        return buckets;
    }

    /**
     * 执行数据处理，比如数据冲突检测
     */
    private void doBefore(List<EventData> items, final DbLoadContext context, final DbLoadData loadData) {
        for (final EventData item : items) {
            boolean filter = interceptor.before(context, item);
            if (!filter) {
                loadData.merge(item);// 进行分类
            }
        }
    }

    private void doLoad(final DbLoadContext context, DbLoadData loadData) {
        // 优先处理delete,可以利用batch优化
        List<List<EventData>> batchDatas = new ArrayList<List<EventData>>();
        for (TableLoadData tableData : loadData.getTables()) {
            if (useBatch) {
                // 优先执行delete语句，针对uniqe更新，一般会进行delete + insert的处理模式，避免并发更新
                batchDatas.addAll(split(tableData.getDeleteDatas()));
            } else {
                // 如果不可以执行batch，则按照单条数据进行并行提交
                // 优先执行delete语句，针对uniqe更新，一般会进行delete + insert的处理模式，避免并发更新
                for (EventData data : tableData.getDeleteDatas()) {
                    batchDatas.add(Arrays.asList(data));
                }
            }
        }

        if (context.getPipeline().getParameters().isDryRun()) {
            doDryRun(context, batchDatas, true);
        } else {
            doTwoPhase(context, batchDatas, true);
        }
        batchDatas.clear();

        // 处理下insert/update
        for (TableLoadData tableData : loadData.getTables()) {
            if (useBatch) {
                // 执行insert + update语句
                batchDatas.addAll(split(tableData.getInsertDatas()));
                batchDatas.addAll(split(tableData.getUpadateDatas()));// 每条记录分为一组，并行加载
            } else {
                // 执行insert + update语句
                for (EventData data : tableData.getInsertDatas()) {
                    batchDatas.add(Arrays.asList(data));
                }
                for (EventData data : tableData.getUpadateDatas()) {
                    batchDatas.add(Arrays.asList(data));
                }
            }
        }

        if (context.getPipeline().getParameters().isDryRun()) {
            doDryRun(context, batchDatas, true);
        } else {
            doTwoPhase(context, batchDatas, true);
        }
        batchDatas.clear();
    }

    /**
     * 将对应的数据按照sql相同进行batch组合
     */
    private List<List<EventData>> split(List<EventData> datas) {
        List<List<EventData>> result = new ArrayList<List<EventData>>();
        if (datas == null || datas.size() == 0) {
            return result;
        } else {
            int[] bits = new int[datas.size()];// 初始化一个标记，用于标明对应的记录是否已分入某个batch
            for (int i = 0; i < bits.length; i++) {
                // 跳过已经被分入batch的
                while (i < bits.length && bits[i] == 1) {
                    i++;
                }

                if (i >= bits.length) { // 已处理完成，退出
                    break;
                }

                // 开始添加batch，最大只加入batchSize个数的对象
                List<EventData> batch = new ArrayList<EventData>();
                bits[i] = 1;
                batch.add(datas.get(i));
                for (int j = i + 1; j < bits.length && batch.size() < batchSize; j++) {
                    if (bits[j] == 0 && canBatch(datas.get(i), datas.get(j))) {
                        batch.add(datas.get(j));
                        bits[j] = 1;// 修改为已加入
                    }
                }
                result.add(batch);
            }

            return result;
        }
    }

    /**
     * 判断两条记录是否可以作为一个batch提交，主要判断sql是否相等. 可优先通过schemaName进行判断
     */
    private boolean canBatch(EventData source, EventData target) {
        // return StringUtils.equals(source.getSchemaName(),
        // target.getSchemaName())
        // && StringUtils.equals(source.getTableName(), target.getTableName())
        // && StringUtils.equals(source.getSql(), target.getSql());
        // return StringUtils.equals(source.getSql(), target.getSql());

        // 因为sqlTemplate构造sql时用了String.intern()的操作，保证相同字符串的引用是同一个，所以可以直接使用==进行判断，提升效率
        return source.getSql() == target.getSql();
    }

    private void doDryRun(DbLoadContext context, List<List<EventData>> totalRows, boolean canBatch) {
        for (List<EventData> rows : totalRows) {
            if (CollectionUtils.isEmpty(rows)) {
                continue; // 过滤空记录
            }

            for (EventData row : rows) {
                processStat(row, context);// 直接记录成功状态
            }

            context.getProcessedDatas().addAll(rows);
        }
    }

    /**
     * 执行ddl的调用，处理逻辑比较简单: 串行调用
     * 
     * @param context
     * @param eventDatas
     */
    private void doDdl(DbLoadContext context, List<EventData> eventDatas) {
        for (final EventData data : eventDatas) {
            DataMedia dataMedia = ConfigHelper.findDataMedia(context.getPipeline(), data.getTableId());
            final DbDialect dbDialect = dbDialectFactory.getDbDialect(context.getIdentity().getPipelineId(),
                (DbMediaSource) dataMedia.getSource());
            Boolean skipDdlException = context.getPipeline().getParameters().getSkipDdlException();
            try {
                Boolean result = dbDialect.getJdbcTemplate().execute(new StatementCallback<Boolean>() {

                    public Boolean doInStatement(Statement stmt) throws SQLException, DataAccessException {
                        Boolean result = false;
                        if (dbDialect instanceof MysqlDialect && StringUtils.isNotEmpty(data.getDdlSchemaName())) {
                            // 如果mysql，执行ddl时，切换到在源库执行的schema上
                            // result &= stmt.execute("use " +
                            // data.getDdlSchemaName());

                            // 解决当数据库名称为关键字如"Order"的时候,会报错,无法同步
                            result &= stmt.execute("use `" + data.getDdlSchemaName() + "`");
                        }
                        result &= stmt.execute(data.getSql());
                        return result;
                    }
                });
                if (result) {
                    context.getProcessedDatas().add(data); // 记录为成功处理的sql
                } else {
                    context.getFailedDatas().add(data);
                }

            } catch (Throwable e) {
                if (skipDdlException) {
                    // do skip
                    logger.warn("skip exception for ddl : {} , caused by {}", data, ExceptionUtils.getFullStackTrace(e));
                } else {
                    throw new LoadException(e);
                }
            }

        }
    }

    /**
     * 首先进行并行执行，出错后转为串行执行
     */
    private void doTwoPhase(DbLoadContext context, List<List<EventData>> totalRows, boolean canBatch) {
        // 预处理下数据
        List<Future<Exception>> results = new ArrayList<Future<Exception>>();
        for (List<EventData> rows : totalRows) {
            if (CollectionUtils.isEmpty(rows)) {
                continue; // 过滤空记录
            }

            results.add(executor.submit(new DbLoadWorker(context, rows, canBatch)));
        }

        boolean partFailed = false;
        for (int i = 0; i < results.size(); i++) {
            Future<Exception> result = results.get(i);
            Exception ex = null;
            try {
                ex = result.get();
                for (EventData data : totalRows.get(i)) {
                    interceptor.after(context, data);// 通知加载完成
                }
            } catch (Exception e) {
                ex = e;
            }

            if (ex != null) {
                logger.warn("##load phase one failed!", ex);
                partFailed = true;
            }
        }

        if (true == partFailed) {
            // if (CollectionUtils.isEmpty(context.getFailedDatas())) {
            // logger.error("##load phase one failed but failedDatas is empty!");
            // return;
            // }

            // 尝试的内容换成phase one跑的所有数据，避免因failed datas计算错误而导致丢数据
            List<EventData> retryEventDatas = new ArrayList<EventData>();
            for (List<EventData> rows : totalRows) {
                retryEventDatas.addAll(rows);
            }

            context.getFailedDatas().clear(); // 清理failed data数据

            // 可能为null，manager老版本数据序列化传输时，因为数据库中没有skipLoadException变量配置
            Boolean skipLoadException = context.getPipeline().getParameters().getSkipLoadException();
            if (skipLoadException != null && skipLoadException) {// 如果设置为允许跳过单条异常，则一条条执行数据load，准确过滤掉出错的记录，并进行日志记录
                for (EventData retryEventData : retryEventDatas) {
                    DbLoadWorker worker = new DbLoadWorker(context, Arrays.asList(retryEventData), false);// 强制设置batch为false
                    try {
                        Exception ex = worker.call();
                        if (ex != null) {
                            // do skip
                            logger.warn("skip exception for data : {} , caused by {}",
                                retryEventData,
                                ExceptionUtils.getFullStackTrace(ex));
                        }
                    } catch (Exception ex) {
                        // do skip
                        logger.warn("skip exception for data : {} , caused by {}",
                            retryEventData,
                            ExceptionUtils.getFullStackTrace(ex));
                    }
                }
            } else {
                // 直接一批进行处理，减少线程调度
                DbLoadWorker worker = new DbLoadWorker(context, retryEventDatas, false);// 强制设置batch为false
                try {
                    Exception ex = worker.call();
                    if (ex != null) {
                        throw ex; // 自己抛自己接
                    }
                } catch (Exception ex) {
                    logger.error("##load phase two failed!", ex);
                    throw new LoadException(ex);
                }
            }

            // 清理failed data数据
            for (EventData data : retryEventDatas) {
                interceptor.after(context, data);// 通知加载完成
            }
        }

    }

    // 调整一下线程池
    private void adjustPoolSize(DbLoadContext context) {
        Pipeline pipeline = context.getPipeline();
        int newPoolSize = pipeline.getParameters().getLoadPoolSize();
        if (newPoolSize != poolSize) {
            poolSize = newPoolSize;
            if (executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
                pool.setCorePoolSize(newPoolSize);
                pool.setMaximumPoolSize(newPoolSize);
            }
        }
    }

    private void adjustConfig(DbLoadContext context) {
        Pipeline pipeline = context.getPipeline();
        this.useBatch = pipeline.getParameters().isUseBatch();
    }

    public void afterPropertiesSet() throws Exception {
        executor = new ThreadPoolExecutor(poolSize,
            poolSize,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue(poolSize * 4),
            new NamedThreadFactory(WORKER_NAME),
            new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void destroy() throws Exception {
        executor.shutdownNow();
    }

    enum ExecuteResult {
        SUCCESS, ERROR, RETRY
    }

    class DbLoadWorker implements Callable<Exception> {

        private DbLoadContext   context;
        private DbDialect       dbDialect;
        private List<EventData> datas;
        private boolean         canBatch;
        private List<EventData> allFailedDatas   = new ArrayList<EventData>();
        private List<EventData> allProcesedDatas = new ArrayList<EventData>();
        private List<EventData> processedDatas   = new ArrayList<EventData>();
        private List<EventData> failedDatas      = new ArrayList<EventData>();

        public DbLoadWorker(DbLoadContext context, List<EventData> datas, boolean canBatch){
            this.context = context;
            this.datas = datas;
            this.canBatch = canBatch;

            EventData data = datas.get(0); // eventData为同一数据库的记录，只取第一条即可
            DataMedia dataMedia = ConfigHelper.findDataMedia(context.getPipeline(), data.getTableId());
            dbDialect = dbDialectFactory.getDbDialect(context.getIdentity().getPipelineId(),
                (DbMediaSource) dataMedia.getSource());

        }

        public Exception call() throws Exception {
            try {
                Thread.currentThread().setName(String.format(WORKER_NAME_FORMAT,
                    context.getPipeline().getId(),
                    context.getPipeline().getName()));
                return doCall();
            } finally {
                Thread.currentThread().setName(WORKER_NAME);
            }
        }

        private Exception doCall() {
            RuntimeException error = null;
            ExecuteResult exeResult = null;
            int index = 0;// 记录下处理成功的记录下标
            for (; index < datas.size();) {
                // 处理数据切分
                final List<EventData> splitDatas = new ArrayList<EventData>();
                if (useBatch && canBatch) {
                    int end = (index + batchSize > datas.size()) ? datas.size() : (index + batchSize);
                    splitDatas.addAll(datas.subList(index, end));
                    index = end;// 移动到下一批次
                } else {
                    splitDatas.add(datas.get(index));
                    index = index + 1;// 移动到下一条
                }

                int retryCount = 0;
                while (true) {
                    try {
                        if (CollectionUtils.isEmpty(failedDatas) == false) {
                            splitDatas.clear();
                            splitDatas.addAll(failedDatas); // 下次重试时，只处理错误的记录
                        } else {
                            failedDatas.addAll(splitDatas); // 先添加为出错记录，可能获取lob,datasource会出错
                        }

                        final LobCreator lobCreator = dbDialect.getLobHandler().getLobCreator();
                        if (useBatch && canBatch) {
                            // 处理batch
                            final String sql = splitDatas.get(0).getSql();
                            int[] affects = new int[splitDatas.size()];
                            affects = (int[]) dbDialect.getTransactionTemplate().execute(new TransactionCallback() {

                                public Object doInTransaction(TransactionStatus status) {
                                    // 初始化一下内容
                                    try {
                                        failedDatas.clear(); // 先清理
                                        processedDatas.clear();
                                        interceptor.transactionBegin(context, splitDatas, dbDialect);
                                        JdbcTemplate template = dbDialect.getJdbcTemplate();
                                        int[] affects = template.batchUpdate(sql, new BatchPreparedStatementSetter() {

                                            public void setValues(PreparedStatement ps, int idx) throws SQLException {
                                                doPreparedStatement(ps, dbDialect, lobCreator, splitDatas.get(idx));
                                            }

                                            public int getBatchSize() {
                                                return splitDatas.size();
                                            }
                                        });
                                        interceptor.transactionEnd(context, splitDatas, dbDialect);
                                        return affects;
                                    } finally {
                                        lobCreator.close();
                                    }
                                }

                            });

                            // 更新统计信息
                            for (int i = 0; i < splitDatas.size(); i++) {
                                processStat(splitDatas.get(i), affects[i], true);
                            }
                        } else {
                            final EventData data = splitDatas.get(0);// 直接取第一条
                            int affect = 0;
                            affect = (Integer) dbDialect.getTransactionTemplate().execute(new TransactionCallback() {

                                public Object doInTransaction(TransactionStatus status) {
                                    try {
                                        failedDatas.clear(); // 先清理
                                        processedDatas.clear();
                                        interceptor.transactionBegin(context, Arrays.asList(data), dbDialect);
                                        JdbcTemplate template = dbDialect.getJdbcTemplate();
                                        int affect = template.update(data.getSql(), new PreparedStatementSetter() {

                                            public void setValues(PreparedStatement ps) throws SQLException {
                                                doPreparedStatement(ps, dbDialect, lobCreator, data);
                                            }
                                        });
                                        interceptor.transactionEnd(context, Arrays.asList(data), dbDialect);
                                        return affect;
                                    } finally {
                                        lobCreator.close();
                                    }
                                }
                            });
                            // 更新统计信息
                            processStat(data, affect, false);
                        }

                        error = null;
                        exeResult = ExecuteResult.SUCCESS;
                    } catch (DeadlockLoserDataAccessException ex) {
                        error = new LoadException(ExceptionUtils.getFullStackTrace(ex),
                            DbLoadDumper.dumpEventDatas(splitDatas));
                        exeResult = ExecuteResult.RETRY;
                    } catch (DataIntegrityViolationException ex) {
                        error = new LoadException(ExceptionUtils.getFullStackTrace(ex),
                            DbLoadDumper.dumpEventDatas(splitDatas));
                        // if (StringUtils.contains(ex.getMessage(),
                        // "ORA-00001")) {
                        // exeResult = ExecuteResult.RETRY;
                        // } else {
                        // exeResult = ExecuteResult.ERROR;
                        // }
                        exeResult = ExecuteResult.ERROR;
                    } catch (RuntimeException ex) {
                        error = new LoadException(ExceptionUtils.getFullStackTrace(ex),
                            DbLoadDumper.dumpEventDatas(splitDatas));
                        exeResult = ExecuteResult.ERROR;
                    } catch (Throwable ex) {
                        error = new LoadException(ExceptionUtils.getFullStackTrace(ex),
                            DbLoadDumper.dumpEventDatas(splitDatas));
                        exeResult = ExecuteResult.ERROR;
                    }

                    if (ExecuteResult.SUCCESS == exeResult) {
                        allFailedDatas.addAll(failedDatas);// 记录一下异常到all记录中
                        allProcesedDatas.addAll(processedDatas);
                        failedDatas.clear();// 清空上一轮的处理
                        processedDatas.clear();
                        break; // do next eventData
                    } else if (ExecuteResult.RETRY == exeResult) {
                        retryCount = retryCount + 1;// 计数一次
                        // 出现异常，理论上当前的批次都会失败
                        processedDatas.clear();
                        failedDatas.clear();
                        failedDatas.addAll(splitDatas);
                        if (retryCount >= retry) {
                            processFailedDatas(index);// 重试已结束，添加出错记录并退出
                            throw new LoadException(String.format("execute [%s] retry %s times failed",
                                context.getIdentity().toString(),
                                retryCount), error);
                        } else {
                            try {
                                int wait = retryCount * retryWait;
                                wait = (wait < retryWait) ? retryWait : wait;
                                Thread.sleep(wait);
                            } catch (InterruptedException ex) {
                                Thread.interrupted();
                                processFailedDatas(index);// 局部处理出错了
                                throw new LoadException(ex);
                            }
                        }
                    } else {
                        // 出现异常，理论上当前的批次都会失败
                        processedDatas.clear();
                        failedDatas.clear();
                        failedDatas.addAll(splitDatas);
                        processFailedDatas(index);// 局部处理出错了
                        throw error;
                    }
                }
            }

            // 记录一下当前处理过程中失败的记录,affect = 0的记录
            context.getFailedDatas().addAll(allFailedDatas);
            context.getProcessedDatas().addAll(allProcesedDatas);
            return null;
        }

        private void doPreparedStatement(PreparedStatement ps, DbDialect dbDialect, LobCreator lobCreator,
                                         EventData data) throws SQLException {
            EventType type = data.getEventType();
            // 注意insert/update语句对应的字段数序都是将主键排在后面
            List<EventColumn> columns = new ArrayList<EventColumn>();
            if (type.isInsert()) {
                columns.addAll(data.getColumns()); // insert为所有字段
                columns.addAll(data.getKeys());
            } else if (type.isDelete()) {
                columns.addAll(data.getKeys());
            } else if (type.isUpdate()) {
                boolean existOldKeys = !CollectionUtils.isEmpty(data.getOldKeys());
                columns.addAll(data.getUpdatedColumns());// 只更新带有isUpdate=true的字段
                if (existOldKeys && dbDialect.isDRDS()) {
                    // DRDS需要区分主键是否有变更
                    columns.addAll(data.getUpdatedKeys());
                } else {
                    columns.addAll(data.getKeys());
                }
                if (existOldKeys) {
                    columns.addAll(data.getOldKeys());
                }
            }

            // 获取一下当前字段名的数据是否必填
            Table table = dbDialect.findTable(data.getSchemaName(), data.getTableName());
            Map<String, Boolean> isRequiredMap = new HashMap<String, Boolean>();
            for (Column tableColumn : table.getColumns()) {
                isRequiredMap.put(StringUtils.lowerCase(tableColumn.getName()), tableColumn.isRequired());
            }

            for (int i = 0; i < columns.size(); i++) {
                int paramIndex = i + 1;
                EventColumn column = columns.get(i);
                int sqlType = column.getColumnType();

                Boolean isRequired = isRequiredMap.get(StringUtils.lowerCase(column.getColumnName()));
                if (isRequired == null) {
                    // 清理一下目标库的表结构,二次检查一下
                    table = dbDialect.findTable(data.getSchemaName(), data.getTableName(), false);
                    isRequiredMap = new HashMap<String, Boolean>();
                    for (Column tableColumn : table.getColumns()) {
                        isRequiredMap.put(StringUtils.lowerCase(tableColumn.getName()), tableColumn.isRequired());
                    }

                    isRequired = isRequiredMap.get(StringUtils.lowerCase(column.getColumnName()));
                    if (isRequired == null) {
                        throw new LoadException(String.format("column name %s is not found in Table[%s]",
                            column.getColumnName(),
                            table.toString()));
                    }
                }

                Object param = null;
                if (dbDialect instanceof MysqlDialect
                    && (sqlType == Types.TIME || sqlType == Types.TIMESTAMP || sqlType == Types.DATE)) {
                    // 解决mysql的0000-00-00 00:00:00问题，直接依赖mysql
                    // driver进行处理，如果转化为Timestamp会出错
                    param = column.getColumnValue();
                } else {
                    param = SqlUtils.stringToSqlValue(column.getColumnValue(),
                        sqlType,
                        isRequired,
                        dbDialect.isEmptyStringNulled());
                }

                try {
                    switch (sqlType) {
                        case Types.CLOB:
                            lobCreator.setClobAsString(ps, paramIndex, (String) param);
                            break;

                        case Types.BLOB:
                            lobCreator.setBlobAsBytes(ps, paramIndex, (byte[]) param);
                            break;
                        case Types.TIME:
                        case Types.TIMESTAMP:
                        case Types.DATE:
                            // 只处理mysql的时间类型，oracle的进行转化处理
                            if (dbDialect instanceof MysqlDialect) {
                                // 解决mysql的0000-00-00 00:00:00问题，直接依赖mysql
                                // driver进行处理，如果转化为Timestamp会出错
                                ps.setObject(paramIndex, param);
                            } else {
                                StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, null, param);
                            }
                            break;
                        case Types.BIT:
                            // 只处理mysql的bit类型，bit最多存储64位，所以需要使用BigInteger进行处理才能不丢精度
                            // mysql driver将bit按照setInt进行处理，会导致数据越界
                            if (dbDialect instanceof MysqlDialect) {
                                StatementCreatorUtils.setParameterValue(ps, paramIndex, Types.DECIMAL, null, param);
                            } else {
                                StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, null, param);
                            }
                            break;
                        default:
                            StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, null, param);
                            break;
                    }
                } catch (SQLException ex) {
                    logger.error("## SetParam error , [pairId={}, sqltype={}, value={}]",
                        new Object[] { data.getPairId(), sqlType, param });
                    throw ex;
                }
            }
        }

        private void processStat(EventData data, int affect, boolean batch) {
            if (batch && (affect < 1 && affect != Statement.SUCCESS_NO_INFO)) {
                failedDatas.add(data); // 记录到错误的临时队列，进行重试处理
            } else if (!batch && affect < 1) {
                failedDatas.add(data);// 记录到错误的临时队列，进行重试处理
            } else {
                processedDatas.add(data); // 记录到成功的临时队列，commit也可能会失败。所以这记录也可能需要进行重试
                DbLoadAction.this.processStat(data, context);
            }
        }

        // 出现异常回滚了，记录一下异常记录
        private void processFailedDatas(int index) {
            allFailedDatas.addAll(failedDatas);// 添加失败记录
            context.getFailedDatas().addAll(allFailedDatas);// 添加历史出错记录
            for (; index < datas.size(); index++) { // 记录一下未处理的数据
                context.getFailedDatas().add(datas.get(index));
            }
            // 这里不需要添加当前成功记录，出现异常后会rollback所有的成功记录，比如processDatas有记录，但在commit出现失败
            // (bugfix)
            allProcesedDatas.addAll(processedDatas);
            context.getProcessedDatas().addAll(allProcesedDatas);// 添加历史成功记录
        }

    }

    private void processStat(EventData data, DbLoadContext context) {
        LoadThroughput throughput = loadStatsTracker.getStat(context.getIdentity());
        LoadCounter counter = throughput.getStat(data.getPairId());
        EventType type = data.getEventType();
        if (type.isInsert()) {
            counter.getInsertCount().incrementAndGet();
        } else if (type.isUpdate()) {
            counter.getUpdateCount().incrementAndGet();
        } else if (type.isDelete()) {
            counter.getDeleteCount().incrementAndGet();
        }

        counter.getRowCount().incrementAndGet();
        counter.getRowSize().addAndGet(calculateSize(data));
    }

    // 大致估算一下row记录的大小
    private long calculateSize(EventData data) {
        // long size = 0L;
        // size += data.getKeys().toString().getBytes().length - 12 -
        // data.getKeys().size() + 1L;
        // size += data.getColumns().toString().getBytes().length - 12 -
        // data.getKeys().size() + 1L;
        // return size;

        // byte[] bytes = JsonUtils.marshalToByte(data);// 走序列化的方式快速计算一下大小
        // return bytes.length;

        return data.getSize();// 数据不做计算，避免影响性能
    }

    // =============== setter / getter ===============

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void setRetryWait(int retryWait) {
        this.retryWait = retryWait;
    }

    public void setInterceptor(LoadInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public void setDbDialectFactory(DbDialectFactory dbDialectFactory) {
        this.dbDialectFactory = dbDialectFactory;
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setLoadStatsTracker(LoadStatsTracker loadStatsTracker) {
        this.loadStatsTracker = loadStatsTracker;
    }

    public void setUseBatch(boolean useBatch) {
        this.useBatch = useBatch;
    }

}
