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

package com.alibaba.otter.node.etl.select.selector.canal;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.alibaba.otter.canal.sink.AbstractCanalEventDownStreamHandler;
import com.alibaba.otter.canal.sink.CanalEventSink;
import com.alibaba.otter.canal.store.model.Event;
import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.shared.arbitrate.ArbitrateEventService;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;

/**
 * 在{@linkplain CanalEventSink}消费数据之前，更新到对应的store中
 * 
 * @author jianghang 2012-7-31 下午03:27:18
 * @version 4.1.0
 */
public class OtterDownStreamHandler extends AbstractCanalEventDownStreamHandler<List<Event>> {

    private static final Logger      logger                   = LoggerFactory.getLogger(OtterDownStreamHandler.class);
    private static final String      DETECTING_FAILED_MESSAGE = "pid:%s canal elapsed %s seconds no data";
    private Long                     pipelineId;
    private ArbitrateEventService    arbitrateEventService;
    // 心跳检查控制mainstem信号
    private ScheduledExecutorService scheduler                = null;
    private ScheduledFuture          future                   = null;
    private AtomicBoolean            working                  = new AtomicBoolean(false);
    private Integer                  detectingIntervalInSeconds;                                                      // 心跳包发送时间
    private volatile Long            lastEventExecuteTime     = 0L;
    // detecting临时数据
    private int                      detectingThresoldCount   = 10;
    private int                      detectingExpCount        = 1;                                                    // 增常趋势
    private AtomicLong               detectingFailedCount     = new AtomicLong(0);                                    // 检测失败的次数
    private AtomicLong               detectingSuccessedCount  = new AtomicLong(0);                                    // 检测成功的次数

    public void stop() {
        super.stop();

        if (working.compareAndSet(true, false)) {
            stopDetecting();
        }

    }

    public List<Event> before(List<Event> events) {
        lastEventExecuteTime = System.currentTimeMillis();// 记录最后一条数据时间

        if (working.compareAndSet(false, true)) {// 第一次有数据时
            startDetecting();
        }

        return super.before(events);
    }

    public List<Event> retry(List<Event> events) {
        lastEventExecuteTime = System.currentTimeMillis();// 记录最后一条数据时间
        return super.retry(events);
    }

    public List<Event> after(List<Event> events) {
        // do nothing
        return super.after(events);
    }

    private void startDetecting() {
        // 直接发送已追上的状态，保持和eromanga兼容处理
        MainStemEventData mainStemData = new MainStemEventData();
        mainStemData.setPipelineId(pipelineId);
        mainStemData.setStatus(MainStemEventData.Status.OVERTAKE);
        arbitrateEventService.mainStemEvent().single(mainStemData);

        // 启动异步线程定时监控，一定会有数据过来
        String schedulerName = String.format("pipelineId = %s , CanalDetecting", String.valueOf(pipelineId));
        scheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory(schedulerName));
        future = scheduler.scheduleAtFixedRate(new Runnable() {

            public void run() {
                try {
                    MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipelineId));
                    // 检查下和最后收到的数据的时间戳，如果超过一定时间没收到，说明canal解析存在问题
                    // (因为会有心跳包数据，理论上时间间隔会小于一定值)
                    if (isDelayed(System.currentTimeMillis(), lastEventExecuteTime)) {
                        notifyFailed();
                    } else {
                        notifySuccessed();
                    }
                } catch (Exception e) {
                    logger.error("heartbeat check failed!", e);
                } finally {
                    MDC.remove(OtterConstants.splitPipelineLogFileKey);
                }

            }
        }, detectingIntervalInSeconds, detectingIntervalInSeconds, TimeUnit.SECONDS);
    }

    private void stopDetecting() {
        ((ScheduledThreadPoolExecutor) scheduler).remove((Runnable) future);
        scheduler.shutdownNow();
    }

    private void notifyFailed() {
        detectingSuccessedCount.set(0);
        long failedCount = detectingFailedCount.incrementAndGet();
        if (failedCount == 1) {
            detectingExpCount = 1;// 系数重置

            notifyMainstemStatus(MainStemEventData.Status.TAKEING);
        }

        if (failedCount >= detectingThresoldCount * detectingExpCount * detectingExpCount) {
            notifyMainstemStatus(MainStemEventData.Status.TAKEING);
            detectingExpCount++; // 系数增大一次

            // 并且发送一次报警信息，系统不太正常了，超过一定时间一次都没有拿到对应的数据
            // 可能出现的情况：
            // 1. 主备发生切换，定位position花费了过久的时间
            // 2. MysqlEventParser工作不正常，一直拿不到数据，比如数据库挂了，但是又没通知其进行主备切换
            TerminEventData errorEventData = new TerminEventData();
            errorEventData.setPipelineId(pipelineId);
            errorEventData.setType(TerminType.WARNING);
            errorEventData.setCode("mainstem");
            errorEventData.setDesc(String.format(DETECTING_FAILED_MESSAGE,
                pipelineId,
                String.valueOf(detectingIntervalInSeconds * failedCount)));
            arbitrateEventService.terminEvent().single(errorEventData);
        }
    }

    private void notifySuccessed() {
        detectingFailedCount.set(0);
        long successedCount = detectingSuccessedCount.incrementAndGet();
        if (successedCount == 1) {
            detectingExpCount = 1;// 系数重置
            notifyMainstemStatus(MainStemEventData.Status.OVERTAKE);
        }

        if (successedCount >= detectingThresoldCount * detectingExpCount * detectingExpCount) {
            detectingExpCount++; // 系数增大一次
            notifyMainstemStatus(MainStemEventData.Status.OVERTAKE);
        }

    }

    private void notifyMainstemStatus(MainStemEventData.Status status) {
        MainStemEventData mainStemData = new MainStemEventData();
        mainStemData.setPipelineId(pipelineId);
        mainStemData.setStatus(status);
        arbitrateEventService.mainStemEvent().single(mainStemData);
    }

    private boolean isDelayed(Long detectingExecuteTime, Long lastExecuteTime) {
        long delayTime = detectingExecuteTime - lastExecuteTime;
        return delayTime > detectingIntervalInSeconds * 2 * 1000;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public void setDetectingIntervalInSeconds(Integer detectingIntervalInSeconds) {
        this.detectingIntervalInSeconds = detectingIntervalInSeconds;
    }

    public void setArbitrateEventService(ArbitrateEventService arbitrateEventService) {
        this.arbitrateEventService = arbitrateEventService;
    }

    public void setDetectingThresoldCount(int detectingThresoldCount) {
        this.detectingThresoldCount = detectingThresoldCount;
    }

}
