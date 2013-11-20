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

package com.alibaba.otter.node.etl.extract.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.oracle.OracleDialect;
import com.alibaba.otter.node.etl.common.db.utils.SqlUtils;
import com.alibaba.otter.node.etl.extract.exceptions.ExtractException;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventColumnIndexComparable;
import com.alibaba.otter.shared.etl.model.EventData;

/**
 * 基于数据库的反查 , 使用多线程技术进行加速处理 {@linkplain DatabaseExtractWorker}
 * 
 * <pre>
 * 说明：
 *  1. 数据反查的总时间 = ( 数据记录数 / (poolsize + 1) ) * 每条记录查询的时间
 *  2. 当其中的一次并行查询出现异常，会立即中断之前的并行查询请求，同时忽略后续的查询直接退出(在出错时快速响应)
 *  3. 编写{@linkplain DatabaseExtractWorker}代码时需注意，在适合的地方响应Thread.currentThread().isInterrupted(),在dbcp连接池和driver代码中是有支持
 *  4. 反查数据库，只会反查update=true的字段，按需反查，因为通过反查之后字段都会变为update=true，不必要的字段会进行数据同步 (modify by ljh at 2012-11-04)
 * </pre>
 * 
 * @author jianghang 2012-4-18 下午04:53:15
 * @version 4.0.2
 */
public class DatabaseExtractor extends AbstractExtractor<DbBatch> implements InitializingBean, DisposableBean {

    private static final String WORKER_NAME        = "DataBaseExtractor";
    private static final String WORKER_NAME_FORMAT = "pipelineId = %s , pipelineName = %s , " + WORKER_NAME;
    private static final Logger logger             = LoggerFactory.getLogger(DatabaseExtractor.class);
    private static final int    DEFAULT_POOL_SIZE  = 5;
    private static final int    retryTimes         = 3;
    private int                 poolSize           = DEFAULT_POOL_SIZE;
    private ExecutorService     executor;

    @Override
    public void extract(DbBatch dbBatch) throws ExtractException {
        Assert.notNull(dbBatch);
        Assert.notNull(dbBatch.getRowBatch());
        // 读取配置
        Pipeline pipeline = getPipeline(dbBatch.getRowBatch().getIdentity().getPipelineId());
        boolean mustDb = pipeline.getParameters().getSyncConsistency().isMedia();
        boolean isRow = pipeline.getParameters().getSyncMode().isRow();// 如果是行记录是必须进行数据库反查
        // 读取一次配置
        adjustPoolSize(pipeline.getParameters().getExtractPoolSize()); // 调整下线程池，Extractor会被池化处理
        ExecutorCompletionService completionService = new ExecutorCompletionService(executor);

        // 进行并发提交
        ExtractException exception = null;
        // 每个表进行处理
        List<DataItem> items = new ArrayList<DataItem>();
        List<Future> futures = new ArrayList<Future>();
        List<EventData> eventDatas = dbBatch.getRowBatch().getDatas();
        for (EventData eventData : eventDatas) {
            if (eventData.getEventType().isDdl()) {
                continue;
            }

            DataItem item = new DataItem(eventData);
            // 针对row模式，需要去检查一下当前是否已经包含row记录的所有字段，如果发现字段不足，则执行一次数据库查询
            boolean flag = mustDb
                           || (eventData.getSyncConsistency() != null && eventData.getSyncConsistency().isMedia());

            // 增加一种case, 针对oracle erosa有时侯结果记录只有主键，没有变更字段，需要做一次反查
            if (!flag && CollectionUtils.isEmpty(eventData.getUpdatedColumns())) {
                DataMedia dataMedia = ConfigHelper.findDataMedia(pipeline, eventData.getTableId());
                if (dataMedia.getSource().getType().isOracle()) {
                    flag |= true;
                    eventData.setRemedy(true);// 针对这类数据，也统一视为补救的操作，可能erosa解析时反查数据库也不存在记录
                }
            }

            if (isRow && !flag) {
                // 提前判断一次，避免进入多线程进行竞争
                // 针对view视图的情况，会有后续再判断一次
                flag = checkNeedDbForRowMode(pipeline, eventData);
            }

            if (flag && (eventData.getEventType().isInsert() || eventData.getEventType().isUpdate())) {// 判断是否需要反查
                Future future = completionService.submit(new DatabaseExtractWorker(pipeline, item), null); // 提交进行并行查询
                if (future.isDone()) {
                    // 立即判断一次，因为使用了CallerRun可能当场跑出结果，针对有异常时快速响应，而不是等跑完所有的才抛异常
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        cancel(futures);// 取消完之后立马退出
                        throw new ExtractException(e);
                    } catch (ExecutionException e) {
                        cancel(futures); // 取消完之后立马退出
                        throw new ExtractException(e);
                    }
                }

                futures.add(future);// 记录一下添加的任务
            }

            items.add(item);// 按顺序添加
        }

        // 开始处理结果
        int index = 0;
        while (index < futures.size()) { // 循环处理发出去的所有任务
            try {
                Future future = completionService.take();// 它也可能被打断
                future.get();
            } catch (InterruptedException e) {
                exception = new ExtractException(e);
                break;// 如何一个future出现了异常，就退出
            } catch (ExecutionException e) {
                exception = new ExtractException(e);
                break;// 如何一个future出现了异常，就退出
            }

            index++;
        }

        if (index < futures.size()) {
            // 小于代表有错误，需要对未完成的记录进行cancel操作，对已完成的结果进行收集，做重复录入过滤记录
            cancel(futures);
            throw exception;
        } else {
            // 全部成功分支, 构造返回结果也要保证原始的顺序
            for (int i = 0; i < items.size(); i++) {
                DataItem item = items.get(i);
                if (item.filter) { // 忽略需要被过滤的数据，比如数据库反查时记录已经不存在
                    eventDatas.remove(item.getEventData());
                }
            }
        }

    }

    private boolean checkNeedDbForRowMode(Pipeline pipeline, EventData eventData) {
        // 获取数据表信息
        DataMedia dataMedia = ConfigHelper.findDataMedia(pipeline, eventData.getTableId());
        DbDialect dbDialect = dbDialectFactory.getDbDialect(pipeline.getId(), (DbMediaSource) dataMedia.getSource());
        Table table = dbDialect.findTable(eventData.getSchemaName(), eventData.getTableName());
        if (table.getColumnCount() == eventData.getColumns().size() + eventData.getKeys().size()) {
            return false;
        } else {
            return true;
        }
    }

    // 取消一下当前正在执行的异步任务
    private void cancel(List<Future> futures) {
        for (int i = 0; i < futures.size(); i++) {
            Future future = futures.get(i);
            if (future.isDone() == false) {
                future.cancel(true);// 中断之前的操作
            }
        }
    }

    // 调整一下线程池
    private void adjustPoolSize(int newPoolSize) {
        if (newPoolSize != poolSize) {
            poolSize = newPoolSize;
            if (executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
                pool.setCorePoolSize(newPoolSize);
                pool.setMaximumPoolSize(newPoolSize);
            }
        }
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

    // 异步处理的结构体
    class DataItem {

        private EventData eventData;
        private boolean   filter = false;

        public DataItem(EventData eventData){
            this.eventData = eventData;
        }

        public EventData getEventData() {
            return eventData;
        }

        public void setEventData(EventData eventData) {
            this.eventData = eventData;
        }

        public boolean isFilter() {
            return filter;
        }

        public void setFilter(boolean filter) {
            this.filter = filter;
        }

    }

    /**
     * 反查数据异步处理单元，反查速度由主方法进行控制
     * 
     * @author jianghang 2012-4-19 下午05:14:18
     * @version 4.0.2
     */
    class DatabaseExtractWorker implements Runnable {

        private final int    event_default_capacity = 1024;                      // 预设值StringBuilder，减少扩容影响
        private String       eventData_format       = null;
        private final String SEP                    = SystemUtils.LINE_SEPARATOR;

        private Pipeline     pipeline;
        private DataItem     item;
        private EventData    eventData;
        {
            eventData_format = "-----------------" + SEP;
            eventData_format += "- PairId: {0} , TableId: {1} " + SEP;
            eventData_format += "-----------------" + SEP;
            eventData_format += "---START" + SEP;
            eventData_format += "---Pks" + SEP;
            eventData_format += "{2}" + SEP;
            eventData_format += "---Sql" + SEP;
            eventData_format += "{3}" + SEP;
            eventData_format += "---END" + SEP;
        }

        public DatabaseExtractWorker(Pipeline pipeline, DataItem item){
            this.pipeline = pipeline;
            this.item = item;
            this.eventData = item.getEventData();
        }

        public void run() {
            try {
                MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipeline.getId()));
                Thread.currentThread().setName(String.format(WORKER_NAME_FORMAT, pipeline.getId(), pipeline.getName()));
                // 获取数据表信息
                DataMedia dataMedia = ConfigHelper.findDataMedia(pipeline, eventData.getTableId());
                DbDialect dbDialect = dbDialectFactory.getDbDialect(pipeline.getId(),
                    (DbMediaSource) dataMedia.getSource());
                Table table = dbDialect.findTable(eventData.getSchemaName(), eventData.getTableName());
                TableData keyTableData = buildTableData(table, eventData.getKeys());

                // oracle类型特殊处理下
                if (dbDialect instanceof OracleDialect) {
                    keyTableData.columnTypes = getOraclePkTypes(table, keyTableData.columnNames);
                }

                boolean needAll = pipeline.getParameters().getSyncMode().isRow()
                                  || (eventData.getSyncMode() != null && eventData.getSyncMode().isRow());

                // 增加一种case, 针对oracle erosa有时侯结果记录只有主键，没有变更字段，需要做一次反查，获取所有字段
                needAll |= CollectionUtils.isEmpty(eventData.getUpdatedColumns())
                           && dataMedia.getSource().getType().isOracle();

                List<DataMediaPair> mediaParis = ConfigHelper.findDataMediaPairByMediaId(pipeline, dataMedia.getId());
                List<String> viewColumnNames = buildMaxColumnsFromColumnPairs(mediaParis, eventData.getKeys());

                // TODO 后续版本测试下
                // if (needAll) {
                // boolean needDb = checkNeedDbForRowMode(table,
                // viewColumnNames, eventData);
                // if (needAll && !needDb) {// 不需要进行反查
                // item.setFilter(false);
                // return;
                // }
                // }

                // modified by ljh at 2012-11-04
                // 反查数据时只反查带update=true标识的数据，因为update=false的记录可能只是进行filter需要用到的数据，不需要反查
                TableData columnTableData = buildTableData(table,
                    eventData.getUpdatedColumns(),
                    needAll,
                    viewColumnNames);

                if (columnTableData.columnNames.length == 0) {
                    // 全主键，不需要进行反查
                } else {
                    List<String> newColumnValues = select(dbDialect,
                        eventData.getSchemaName(),
                        eventData.getTableName(),
                        keyTableData,
                        columnTableData);

                    if (newColumnValues == null) {
                        // miss from db
                        // 设置为filter=true，可能存在丢数据的风险.
                        // 比如针对源库发生主备切换，otter反查的是备库，查询不到对应的记录
                        // item.setFilter(true);

                        // 针对需要自定义反查数据库的，允许忽略
                        // a. 自由门触发的数据，不存在时可以忽略
                        // b. 回环补救算法触发的数据，不存在时可以忽略
                        boolean needFilter = eventData.isRemedy() || pipeline.getParameters().getSkipNoRow();
                        item.setFilter(needFilter);

                        // 判断主键是否有变更，如果变更了，就原样返回item
                        int index = 0;
                        for (EventColumn oldKey : eventData.getOldKeys()) {
                            if (!oldKey.equals(eventData.getKeys().get(index))) {
                                item.setFilter(false);
                                break;
                            }
                        }
                    } else {
                        // 构造反查的返回结果
                        List<EventColumn> newEventColumns = new ArrayList<EventColumn>();
                        for (int i = 0; i < newColumnValues.size(); i++) {
                            EventColumn column = new EventColumn();
                            column.setIndex(columnTableData.indexs[i]);
                            column.setColumnName(columnTableData.columnNames[i]);
                            column.setColumnType(columnTableData.columnTypes[i]);
                            column.setNull(newColumnValues.get(i) == null);
                            column.setColumnValue(newColumnValues.get(i));
                            column.setUpdate(true);
                            newEventColumns.add(column);
                        }

                        // 处理下columns中不在反查字段内的字段列表
                        for (EventColumn column : eventData.getColumns()) {
                            boolean override = false;
                            for (EventColumn newEventColumn : newEventColumns) {
                                if (StringUtils.equalsIgnoreCase(newEventColumn.getColumnName(), column.getColumnName())) {
                                    override = true;
                                    break;
                                }
                            }

                            if (!override) {// 针对newcolumns不存在的记录进行添加
                                newEventColumns.add(column);
                            }
                        }

                        Collections.sort(newEventColumns, new EventColumnIndexComparable()); // 重新排个序
                        eventData.setColumns(newEventColumns);
                    }
                }
            } catch (InterruptedException e) {
                // ignore
            } finally {
                Thread.currentThread().setName(WORKER_NAME);
                MDC.remove(OtterConstants.splitPipelineLogFileKey);
            }
        }

        /**
         * 根据视图同步定义的columnPair，获取需要反查的字段列表，不包括主键
         */
        private List<String> buildMaxColumnsFromColumnPairs(List<DataMediaPair> mediaPairs, List<EventColumn> pks) {
            Set<String> allColumns = new HashSet<String>();
            Map<String, EventColumn> pkMap = new HashMap<String, EventColumn>(pks.size(), 1f);
            for (EventColumn pk : pks) {
                pkMap.put(StringUtils.lowerCase(pk.getColumnName()), pk);
            }

            for (DataMediaPair mediaPair : mediaPairs) {// 一个源库可以对应多个目标，多路复制
                List<ColumnPair> columnPairs = mediaPair.getColumnPairs();

                if (CollectionUtils.isEmpty(columnPairs) || mediaPair.getColumnPairMode().isExclude()) {
                    // 1. 如果有一个没有视图定义，说明需要所有字段
                    // 2. 如果有一个表存在exclude模式，简单处理，直接反查所有字段，到后面进行过滤
                    return new ArrayList<String>(); // 返回空集合，代表没有view
                                                    // filter，需要所有字段
                } else {
                    for (ColumnPair columnPair : columnPairs) {
                        String columnName = columnPair.getSourceColumn().getName();
                        if (!pkMap.containsKey(StringUtils.lowerCase(columnName))) {
                            allColumns.add(columnPair.getSourceColumn().getName());// 加入的为非主键
                        }
                    }
                }
            }

            return new ArrayList<String>(allColumns);
        }

        private List<String> select(DbDialect dbDialect, String schemaName, String tableName, TableData keyTableData,
                                    TableData columnTableData) throws InterruptedException {
            String selectSql = dbDialect.getSqlTemplate().getSelectSql(schemaName,
                tableName,
                keyTableData.columnNames,
                columnTableData.columnNames);
            Exception exception = null;
            for (int i = 0; i < retryTimes; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException(); // 退出
                }

                try {
                    List<List<String>> result = dbDialect.getJdbcTemplate().query(selectSql,
                        keyTableData.columnValues,
                        keyTableData.columnTypes,
                        new RowDataMapper(columnTableData.columnTypes));
                    if (CollectionUtils.isEmpty(result)) {
                        logger.warn("the mediaName = {}.{} not has rowdate in db \n {}", new Object[] { schemaName,
                                tableName, dumpEventData(eventData, selectSql) });
                        return null;
                    } else {
                        return result.get(0);
                    }

                } catch (Exception e) {
                    exception = e;
                    logger.warn("retry [" + (i + 1) + "] failed", e);
                }
            }

            throw new RuntimeException("db extract failed , data:\n " + dumpEventData(eventData, selectSql), exception);
        }

        /**
         * oracle的erosa获取的字段类型，没有转换成jdbc的类型，所以需要手工转一下.
         */
        private int[] getOraclePkTypes(Table table, String[] pkNames) {
            Column[] columns = table.getColumns();
            List<Integer> pkTypes = new ArrayList<Integer>();
            for (String pkName : pkNames) {
                for (Column column : columns) {
                    if (column.getName().equalsIgnoreCase(pkName)) {
                        pkTypes.add(column.getTypeCode());
                    }
                }
            }
            int[] types = new int[pkTypes.size()];
            for (int i = 0; i < types.length; i++) {
                types[i] = pkTypes.get(i);
            }
            return types;
        }

        @SuppressWarnings("unused")
        private boolean checkNeedDbForRowMode(Table table, List<String> viewColumns, EventData eventData) {
            if (viewColumns.size() != 0) {// 说明有视图
                if (viewColumns.size() != eventData.getColumns().size()) {
                    return true;
                }

                // 检查一下当前是否所有字段都在view字段列表里
                for (EventColumn column : eventData.getColumns()) {
                    if (!viewColumns.contains(column.getColumnName())) {
                        return true;
                    }
                }

                return false;
            } else {
                if (table.getColumnCount() == eventData.getColumns().size() + eventData.getKeys().size()) {
                    return false;
                } else {
                    return true;
                }
            }
        }

        /**
         * 构建数据库主键字段的信息
         */
        private TableData buildTableData(Table table, List<EventColumn> keys) {
            Column[] tableColumns = table.getColumns();

            TableData data = new TableData();
            data.indexs = new int[keys.size()];
            data.columnNames = new String[keys.size()];
            data.columnTypes = new int[keys.size()];
            data.columnValues = new Object[keys.size()];

            int i = 0;
            int index = 0;
            for (EventColumn keyColumn : keys) {
                for (Column tableColumn : tableColumns) {
                    if (StringUtils.equalsIgnoreCase(keyColumn.getColumnName(), tableColumn.getName())) {
                        data.indexs[i] = index;
                        data.columnNames[i] = tableColumn.getName();
                        data.columnTypes[i] = tableColumn.getTypeCode();
                        data.columnValues[i] = SqlUtils.stringToSqlValue(keyColumn.getColumnValue(),
                            tableColumn.getTypeCode(),
                            tableColumn.isRequired(),
                            false);

                        i++;
                        break;
                    }
                    index++;
                }
            }

            if (i != keys.size()) {
                throw new ExtractException("keys is not found in table " + table.toString() + " keys : "
                                           + dumpEventColumn(keys));
            }
            return data;
        }

        /**
         * 构建数据库非主键字段的信息
         */
        private TableData buildTableData(Table table, List<EventColumn> columns, boolean needAll,
                                         List<String> viewColumnNames) {
            Column[] tableColumns = table.getColumns();
            List<Column> noPkcolumns = new ArrayList<Column>();
            for (Column tableColumn : tableColumns) {
                if (!tableColumn.isPrimaryKey()) {
                    noPkcolumns.add(tableColumn);
                }
            }

            TableData data = new TableData();
            int size = columns.size();
            if (needAll) {
                size = viewColumnNames.size() != 0 ? viewColumnNames.size() : noPkcolumns.size();// 如果view不为空就使用view作为反查字段
            }

            data.indexs = new int[size];
            data.columnNames = new String[size];
            data.columnTypes = new int[size];
            data.columnValues = new Object[size];

            int i = 0;
            if (needAll) {
                int index = 0;
                if (viewColumnNames.size() != 0) { // 存在视图定义
                    for (Column tableColumn : tableColumns) {
                        if (viewColumnNames.contains(tableColumn.getName())) {// 只放入在view中定义的
                            data.indexs[i] = index;// 计算下下标
                            data.columnNames[i] = tableColumn.getName();
                            data.columnTypes[i] = tableColumn.getTypeCode();
                            i++;
                        }

                        index++;
                    }
                } else {
                    for (Column tableColumn : tableColumns) {
                        if (!tableColumn.isPrimaryKey()) {
                            data.indexs[i] = index;// 计算下下标
                            data.columnNames[i] = tableColumn.getName();
                            data.columnTypes[i] = tableColumn.getTypeCode();
                            i++;
                        }
                        index++;
                    }
                }
            } else {
                for (EventColumn column : columns) {
                    int index = 0;
                    for (Column tableColumn : tableColumns) {
                        if (StringUtils.equalsIgnoreCase(column.getColumnName(), tableColumn.getName())) {
                            data.indexs[i] = index;// 计算下下标
                            data.columnNames[i] = tableColumn.getName();
                            data.columnTypes[i] = tableColumn.getTypeCode();

                            i++;
                            break;
                        }
                        index++;
                    }
                }

                if (i != columns.size()) {
                    throw new ExtractException("columns is not found in table " + table.toString() + " columns : "
                                               + dumpEventColumn(columns));
                }
            }

            return data;
        }

        private String dumpEventData(EventData eventData, String selectSql) {
            return MessageFormat.format(eventData_format,
                eventData.getPairId(),
                eventData.getTableId(),
                dumpEventColumn(eventData.getKeys()),
                "\t" + selectSql);
        }

        private String dumpEventColumn(List<EventColumn> columns) {
            StringBuilder builder = new StringBuilder(event_default_capacity);
            int size = columns.size();
            for (int i = 0; i < size; i++) {
                EventColumn column = columns.get(i);
                builder.append("\t").append(column.toString());
                if (i < columns.size() - 1) {
                    builder.append(SEP);
                }
            }
            return builder.toString();
        }

    }

    /**
     * 数据库处理对象
     */
    class TableData {

        int[]    indexs;
        String[] columnNames;
        int[]    columnTypes;
        Object[] columnValues;
    }

    /**
     * 数据库反查的结果处理
     */
    class RowDataMapper implements RowMapper {

        private int[] columnTypes;

        public RowDataMapper(int[] columnTypes){
            this.columnTypes = columnTypes;
        }

        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            List<String> result = new ArrayList<String>();
            for (int i = 0; i < columnTypes.length; i++) {
                try {
                    String value = SqlUtils.sqlValueToString(rs, i + 1, columnTypes[i]);
                    result.add(value);
                } catch (Exception e) {
                    throw new ExtractException("ERROR ## get columnName has an error", e);
                }
            }
            return result;
        }
    }

    // ============================ setter / getter =========================

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

}
