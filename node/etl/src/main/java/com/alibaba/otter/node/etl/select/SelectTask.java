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

package com.alibaba.otter.node.etl.select;

import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.canal.common.CanalException;
import com.alibaba.otter.node.common.statistics.StatisticsClientService;
import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.common.jmx.StageAggregation.AggregationItem;
import com.alibaba.otter.node.etl.common.pipe.PipeKey;
import com.alibaba.otter.node.etl.common.task.GlobalTask;
import com.alibaba.otter.node.etl.extract.SetlFuture;
import com.alibaba.otter.node.etl.select.exceptions.SelectException;
import com.alibaba.otter.node.etl.select.selector.Message;
import com.alibaba.otter.node.etl.select.selector.OtterSelector;
import com.alibaba.otter.node.etl.select.selector.OtterSelectorFactory;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.common.model.statistics.delay.DelayCount;
import com.alibaba.otter.shared.common.utils.lock.BooleanMutex;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

/**
 * select流处理模式的实现版本
 * 
 * <pre>
 * 调度模型：
 * 1. 正常运行调度流程 
 * 假如并行度为3
 * ----------------------------------------------------------->时间轴
 * | ProcessSelect
 * --> 1 
 *      --> 2
 *           -->3
 *               --> 1
 * | ProcessTermin
 *     ---> 1 ack
 *          ----> 2ack
 *                 ---> 3ack
 * a. ProcessSelect拿到数据后，丢入pool池进行异步处理，并通知ProcessTermin顺序接收termin信号
 * b. 同一时间在s/e/t/l流水线上的数据受并行度控制，满了就会阻塞ProcessSelect，避免取过多的数据，只会多取一份，等待其中一个s/e/t/l完成
 * c. ProcessTermin接受到termin信号
 *    i. 会严格按照发出去的batchId/processId进行对比，发现不匹配，发起rollback操作.
 *    ii. 会根据terminType判断这一批数据是否处理成功，如果发现不成功，发起rollback操作
 * 
 * 2. 异常调度流程
 * 假如并行度为3
 * |-->1 --> 2 -->3(ing)
 * a. 当第1份数据,ProcessTermin发现需要rollback，此时需要回滚2,3份数据的批次. (可能第2,3份数据还未提交到s/e/t/l调度中)
 *    i. 如果2批次数据已经提交，等待2批次termin信号的返回，此时需要阻塞ProcessSelect，避免再取新数据
 *    ii. 如果第2批次数据未提交，直接rollback数据，不再进入s/e/t/l调度流程
 * b. 当所有批次都已经处理完成，再通知ProcessSelect启动 (注意：这里会避免rollback和get并发操作，会造成数据不一致)
 * 
 * 3. 热备机制
 * a. Select主线程会一直监听mainstem的信号，一旦抢占成功，则启动ProcessSelect/ProcessTermin线程
 * b. ProcessSelect/ProcessTermin在处理过程中，会检查一下当前节点是否为抢占mainstem成功的节点，如果发现不是，立马停止，继续监听mainstem
 * c. ProcessSelect进行get数据之前，会等到ProcessTermin会读取未被处理过termin信号，对上一次的selector进行ack/rollback处理
 *      i. 注意：ProcessSelect进行get数据时，需要保证batch/termin/get操作状态保持一致，必须都处于同一个数据点上
 * </pre>
 * 
 * @author jianghang 2012-7-31 下午05:39:06
 * @version 4.1.0
 */
public class SelectTask extends GlobalTask {

    // 运行调度控制
    private volatile boolean           isStart          = false;
    // 运行
    private StatisticsClientService    statisticsClientService;
    private OtterSelectorFactory       otterSelectorFactory;
    private OtterSelector<Message>     otterSelector;
    private ExecutorService            executor;
    private BlockingQueue<BatchTermin> batchBuffer      = new LinkedBlockingQueue<BatchTermin>(50); // 设置有界队列，避免小batch处理太多
    private boolean                    needCheck        = false;
    private BooleanMutex               canStartSelector = new BooleanMutex(false);                 // 非常轻量的一个阻塞式实现，调用成本低
    private AtomicInteger              rversion         = new AtomicInteger(0);
    private long                       lastResetTime    = new Date().getTime();

    public SelectTask(Long pipelineId){
        super(pipelineId);
    }

    public void run() {
        MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipelineId));
        try {
            while (running) {
                try {
                    if (isStart) {
                        boolean working = arbitrateEventService.mainStemEvent().check(pipelineId);
                        if (!working) {
                            stopup(false);
                        }

                        LockSupport.parkNanos(5 * 1000 * 1000L * 1000L); // 5秒钟检查一次
                    } else {
                        startup();
                    }
                } catch (Throwable e) {
                    if (isInterrupt(e)) {
                        logger.info("INFO ## select is interrupt", e);
                        return;
                    } else {
                        logger.warn("WARN ## select is failed.", e);
                        sendRollbackTermin(pipelineId, e);

                        // sleep 10秒再进行重试
                        try {
                            Thread.sleep(10 * 1000);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
            }
        } finally {
            arbitrateEventService.mainStemEvent().release(pipelineId);
        }
    }

    /**
     * 尝试启动，需要进行mainstem竞争，拿到锁之后才可以进行下一步
     * 
     * @throws InterruptedException
     */
    private void startup() throws InterruptedException {
        try {
            arbitrateEventService.mainStemEvent().await(pipelineId);
        } catch (Throwable e) {
            if (isInterrupt(e)) {
                logger.info("INFO ## this node is interrupt", e);
            } else {
                logger.warn("WARN ## this node is crashed.", e);
            }
            arbitrateEventService.mainStemEvent().release(pipelineId);
            return;
        }

        executor = Executors.newFixedThreadPool(2); // 启动两个线程
        // 启动selector
        otterSelector = otterSelectorFactory.getSelector(pipelineId); // 获取对应的selector
        otterSelector.start();

        canStartSelector.set(false);// 初始化为false
        startProcessTermin();
        startProcessSelect();

        isStart = true;
    }

    private synchronized void stopup(boolean needInterrut) throws InterruptedException {
        if (isStart) {
            if (executor != null) {
                executor.shutdownNow();
            }

            if (otterSelector != null && otterSelector.isStart()) {
                otterSelector.stop();
            }

            if (needInterrut) {
                throw new InterruptedException();// 抛异常，退出自己
            }

            isStart = false;
        }
    }

    /**
     * 执行数据分发工作
     */
    private void startProcessSelect() {
        executor.submit(new Runnable() {

            public void run() {
                MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipelineId));
                String currentName = Thread.currentThread().getName();
                Thread.currentThread().setName(createTaskName(pipelineId, "ProcessSelect"));
                try {
                    processSelect();
                } finally {
                    Thread.currentThread().setName(currentName);
                    MDC.remove(OtterConstants.splitPipelineLogFileKey);
                }
            }
        });

    }

    private void processSelect() {
        while (running) {
            try {
                // 等待ProcessTermin exhaust，会阻塞
                // ProcessTermin发现出现rollback，会立即通知暂停，比分布式permit及时性高
                canStartSelector.get();

                // 判断当前是否为工作节点，S模块不能出现双节点工作，selector容易出现数据错乱
                if (needCheck) {
                    checkContinueWork();
                }

                // 出现阻塞挂起时，等待mananger处理完成，解挂开启同步
                arbitrateEventService.toolEvent().waitForPermit(pipelineId);// 出现rollback后能及时停住

                // 使用startVersion要解决的一个问题：出现rollback时，尽可能判断取出来的数据是rollback前还是rollback后，想办法丢弃rollback前的数据。
                // (因为出现rollback，之前取出去的几个批次的数据其实是没有执行成功，get取出来的数据会是其后一批数据，如果不丢弃的话，会出现后面的数据先执行，然后又回到出错的点，再执行一遍)
                // int startVersion = rversion.get();
                Message gotMessage = otterSelector.selector();

                // modify by ljh at 2012-09-10，startVersion获取操作应该放在拿到数据之后
                // 放在前面 : (遇到一个并发bug)
                // // a.
                // 先拿startVersion，再获取数据，在拿数据过程中rollback开始并完成了，导致selector返回时数据已经取到了末尾
                // // b. 在进行version判断时发现已经有变化，导致又触发一次拿数据的过程，此时的get
                // cursor已经到队列的末尾，拿不出任何数据，所以出现死等情况
                // 放在后面 : (一点点瑕疵)
                // // a.
                // 并发操作rollback和selector时，针对拿到rollback前的老数据，此时startVersion还未初始化，导致判断不出出现过rollback操作，后面的变更数据会提前同步
                // (概率性会比较高，取决于selector和初始化startVersion的时间间隔)
                int startVersion = rversion.get();

                if (canStartSelector.state() == false) { // 是否出现异常
                    // 回滚在出现异常的瞬间，拿出来的数据，因为otterSelector.selector()会循环，可能出现了rollback，其还未感知到
                    rollback(gotMessage.getId());
                    continue;
                }

                if (CollectionUtils.isEmpty(gotMessage.getDatas())) {// 处理下空数据，也得更新下游标，可能是回环数据被过滤掉
                    // 添加到待响应的buffer列表，不需要await termin信号，因为没启动过s/e/t/l流程
                    batchBuffer.put(new BatchTermin(gotMessage.getId(), false));
                    continue;
                }

                final EtlEventData etlEventData = arbitrateEventService.selectEvent().await(pipelineId);
                if (rversion.get() != startVersion) {// 说明存在过变化，中间出现过rollback，需要丢弃该数据
                    logger.warn("rollback happend , should skip this data and get new message.");
                    canStartSelector.get();// 确认一下rollback是否完成
                    gotMessage = otterSelector.selector();// 这时不管有没有数据，都需要执行一次s/e/t/l
                }

                final Message message = gotMessage;
                final BatchTermin batchTermin = new BatchTermin(message.getId(), etlEventData.getProcessId());
                batchBuffer.put(batchTermin); // 添加到待响应的buffer列表
                Runnable task = new Runnable() {

                    public void run() {
                        // 设置profiling信息
                        boolean profiling = isProfiling();
                        Long profilingStartTime = null;
                        if (profiling) {
                            profilingStartTime = System.currentTimeMillis();
                        }

                        MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipelineId));
                        String currentName = Thread.currentThread().getName();
                        Thread.currentThread().setName(createTaskName(pipelineId, "SelectWorker"));
                        try {
                            pipeline = configClientService.findPipeline(pipelineId);
                            List<EventData> eventData = message.getDatas();
                            long startTime = etlEventData.getStartTime();
                            if (!CollectionUtils.isEmpty(eventData)) {
                                startTime = eventData.get(0).getExecuteTime();
                            }

                            Channel channel = configClientService.findChannelByPipelineId(pipelineId);
                            RowBatch rowBatch = new RowBatch();
                            // 构造唯一标识
                            Identity identity = new Identity();
                            identity.setChannelId(channel.getId());
                            identity.setPipelineId(pipelineId);
                            identity.setProcessId(etlEventData.getProcessId());
                            rowBatch.setIdentity(identity);
                            // 进行数据合并
                            for (EventData data : eventData) {
                                rowBatch.merge(data);
                            }

                            long nextNodeId = etlEventData.getNextNid();
                            List<PipeKey> pipeKeys = rowDataPipeDelegate.put(new DbBatch(rowBatch), nextNodeId);
                            etlEventData.setDesc(pipeKeys);
                            etlEventData.setNumber((long) eventData.size());
                            etlEventData.setFirstTime(startTime); // 使用原始数据的第一条
                            etlEventData.setBatchId(message.getId());

                            if (profiling) {
                                Long profilingEndTime = System.currentTimeMillis();
                                stageAggregationCollector.push(pipelineId,
                                    StageType.SELECT,
                                    new AggregationItem(profilingStartTime, profilingEndTime));
                            }
                            arbitrateEventService.selectEvent().single(etlEventData);
                        } catch (Throwable e) {
                            if (!isInterrupt(e)) {
                                logger.error(String.format("[%s] selectwork executor is error! data:%s",
                                    pipelineId,
                                    etlEventData), e);
                                sendRollbackTermin(pipelineId, e);
                            } else {
                                logger.info(String.format("[%s] selectwork executor is interrrupt! data:%s",
                                    pipelineId,
                                    etlEventData), e);
                            }
                        } finally {
                            Thread.currentThread().setName(currentName);
                            MDC.remove(OtterConstants.splitPipelineLogFileKey);
                        }
                    }
                };

                // 构造pending任务，可在关闭线程时退出任务
                SetlFuture extractFuture = new SetlFuture(StageType.SELECT,
                    etlEventData.getProcessId(),
                    pendingFuture,
                    task);
                executorService.execute(extractFuture);

            } catch (Throwable e) {
                if (!isInterrupt(e)) {
                    logger.error(String.format("[%s] selectTask is error!", pipelineId), e);
                    sendRollbackTermin(pipelineId, e);
                } else {
                    logger.info(String.format("[%s] selectTask is interrrupt!", pipelineId), e);
                    return;
                }
            }
        }
    }

    private void startProcessTermin() {
        executor.submit(new Runnable() {

            public void run() {
                MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipelineId));
                String currentName = Thread.currentThread().getName();
                Thread.currentThread().setName(createTaskName(pipelineId, "ProcessTermin"));
                try {
                    boolean lastStatus = true;
                    while (running) {
                        try {
                            // 处理下上一次非正常退出后，未被处理的termin信息
                            lastStatus = true;
                            // 消费掉所有的termin，不管成功还是失败，都回滚canal中的数据，会有重复
                            arbitrateEventService.terminEvent().exhaust(pipelineId);

                            batchBuffer.clear();// 清空上一次的待处理的batch记录，因为所有的batch都会被rollback掉

                            // 开始处理新的termin数据
                            while (running) {
                                if (batchBuffer.size() == 0) {
                                    // termin任务已经处理完成，可以通知selector重新开始拉数据
                                    if (canStartSelector.state() == false) {
                                        otterSelector.rollback();// rollback一下所有节点，确保所有的节点都被ack，包括被预取出来的数据
                                    }

                                    lastStatus = true;
                                    canStartSelector.set(true);
                                }

                                BatchTermin batch = batchBuffer.take();
                                logger.info("start process termin : {}", batch.toString());
                                if (batch.isNeedWait()) {
                                    lastStatus = processTermin(lastStatus, batch.getBatchId(), batch.getProcessId());
                                } else {
                                    // 不需要wait的批次，直接以上一个batch的结果决定是否ack
                                    if (lastStatus) {
                                        ack(batch.getBatchId());
                                        sendDelayReset(pipelineId);
                                    } else {
                                        rollback(batch.getBatchId());// 会阻塞selector等待所有batch的rollback操作完成
                                    }
                                }

                                logger.info("end process termin : {}  result : {}", batch.toString(), lastStatus);
                            }
                        } catch (CanalException e) {// 捕获可处理的异常，进行retry,基本可自行恢复
                            logger.info(String.format("[%s] ProcessTermin has an error! retry...", pipelineId), e);
                            notifyRollback();
                        } catch (SelectException e) {// 捕获可处理的异常，进行retry,基本可自行恢复
                            logger.info(String.format("[%s] ProcessTermin has an error! retry...", pipelineId), e);
                            notifyRollback();
                        } catch (Throwable e) {
                            if (isInterrupt(e)) {
                                logger.info(String.format("[%s] ProcessTermin is interrupted!", pipelineId), e);
                                return;
                            } else {
                                logger.error(String.format("[%s] ProcessTermin is error!", pipelineId), e);
                                notifyRollback();
                                sendRollbackTermin(pipelineId, e);
                            }
                        }

                        try {
                            Thread.sleep(30000); // sleep 30，等待termin信号都ready
                        } catch (InterruptedException e) {
                        }
                    }
                } finally {
                    Thread.currentThread().setName(currentName);
                    MDC.remove(OtterConstants.splitPipelineLogFileKey);
                }
            }

        });
    }

    private boolean processTermin(boolean lastStatus, Long batchId, Long processId) throws InterruptedException {
        int retry = 0;
        SelectException exception = null;
        TerminEventData terminData = null;
        while (retry++ < 30) {
            // 因为存在网络因素，而且在Load进行termin处理时，因为是异步处理，有一定的概率会出现termin不按顺序过来
            terminData = arbitrateEventService.terminEvent().await(pipelineId);
            Long terminBatchId = terminData.getBatchId();
            Long terminProcessId = terminData.getProcessId();

            if (terminBatchId == null && processId != -1L && !processId.equals(terminProcessId)) {
                // 针对manager发起rollback，terminBatchId可能为null，需要特殊处理下
                exception = new SelectException("unmatched processId, SelectTask batchId = " + batchId
                                                + " processId = " + processId + " and Termin Event: "
                                                + terminData.toString());
                Thread.sleep(1000); // sleep 1秒，等新的数据包
            } else if (terminBatchId != null && batchId != -1L && !batchId.equals(terminBatchId)) {
                exception = new SelectException("unmatched terminId, SelectTask batchId = " + batchId + " processId = "
                                                + processId + " and Termin Event: " + terminData.toString());
                Thread.sleep(1000); // sleep 1秒，等新的数据包
            } else {
                exception = null; // batchId/processId对上了，退出
                break;
            }
        }

        if (exception != null) {
            throw exception;
        }

        if (needCheck) {
            checkContinueWork();
        }

        boolean status = terminData.getType().isNormal();
        if (lastStatus == false && status == true) {
            // 上一批失败，这一批成功，说明调度有问题
            throw new SelectException(String.format("last status is rollback , but now [batchId:%d , processId:%d] is ack",
                batchId,
                terminData.getProcessId()));
        }

        if (terminData.getType().isNormal()) {
            ack(batchId);
            sendDelayStat(pipelineId, terminData.getEndTime(), terminData.getFirstTime());
        } else {
            rollback(batchId);
        }

        arbitrateEventService.terminEvent().ack(terminData); // 先发送对应的数据
        return status;
    }

    private void rollback(Long batchId) {
        notifyRollback();
        // otterSelector.rollback(batchId);
        otterSelector.rollback();// 一旦出错，rollback所有的mark，避免拿出后面的数据进行同步
    }

    private void ack(Long batchId) {
        canStartSelector.set(true);
        otterSelector.ack(batchId);
    }

    private void notifyRollback() {
        canStartSelector.set(false);
        rversion.incrementAndGet();// 变更一下版本
    }

    /**
     * 检查一下是否需要继续工作，因为mainstem代表一个节点是否为工作节点，一旦出现断网，另一个节点就会启用。此时会出现双节点同时工作，
     * 所以需要做一个检查
     */
    private void checkContinueWork() throws InterruptedException {
        boolean working = arbitrateEventService.mainStemEvent().check(pipelineId);
        if (!working) {
            logger.warn("mainstem is not run in this node");
            stopup(true);
        }

    }

    public void shutdown() {
        super.shutdown();

        if (executor != null) {
            executor.shutdownNow();
        }

        if (otterSelector != null && otterSelector.isStart()) {
            otterSelector.stop();
        }
    }

    public static class BatchTermin {

        private Long    batchId   = -1L;
        private Long    processId = -1L;
        private boolean needWait  = true;

        public BatchTermin(Long batchId, Long processId){
            this(batchId, processId, true);
        }

        public BatchTermin(Long batchId, boolean needWait){
            this(batchId, -1L, needWait);
        }

        public BatchTermin(Long batchId, Long processId, boolean needWait){
            this.batchId = batchId;
            this.processId = processId;
            this.needWait = needWait;
        }

        public Long getBatchId() {
            return batchId;
        }

        public void setBatchId(Long batchId) {
            this.batchId = batchId;
        }

        public Long getProcessId() {
            return processId;
        }

        public void setProcessId(Long processId) {
            this.processId = processId;
        }

        public boolean isNeedWait() {
            return needWait;
        }

        public void setNeedWait(boolean needWait) {
            this.needWait = needWait;
        }

        @Override
        public String toString() {
            return "BatchTermin [batchId=" + batchId + ", needWait=" + needWait + ", processId=" + processId + "]";
        }

    }

    private void sendDelayStat(long pipelineId, Long endTime, Long startTime) {
        DelayCount delayCount = new DelayCount();
        delayCount.setPipelineId(pipelineId);
        delayCount.setNumber(0L);// 不再统计delayNumber
        if (startTime != null && endTime != null) {
            delayCount.setTime(endTime - startTime);// 以后改造成获取数据库的sysdate/now()
        }

        statisticsClientService.sendResetDelayCount(delayCount);
    }

    private void sendDelayReset(long pipelineId) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime > 60 * 1000) {
            // 60秒向manager推送一次配置
            lastResetTime = currentTime;
            DelayCount delayCount = new DelayCount();
            delayCount.setPipelineId(pipelineId);
            delayCount.setNumber(0L);
            long delayTime = currentTime - otterSelector.lastEntryTime();
            delayCount.setTime(delayTime);
            statisticsClientService.sendResetDelayCount(delayCount);
        }
    }

    // ======================= setter / getter ===================

    public void setOtterSelectorFactory(OtterSelectorFactory otterSelectorFactory) {
        this.otterSelectorFactory = otterSelectorFactory;
    }

    public void setStatisticsClientService(StatisticsClientService statisticsClientService) {
        this.statisticsClientService = statisticsClientService;
    }

}
