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

package com.alibaba.otter.node.common.statistics.impl;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.node.common.communication.NodeCommmunicationClient;
import com.alibaba.otter.node.common.statistics.StatisticsClientService;
import com.alibaba.otter.shared.common.model.statistics.delay.DelayCount;
import com.alibaba.otter.shared.common.model.statistics.table.TableStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.alibaba.otter.shared.communication.core.model.Callback;
import com.alibaba.otter.shared.communication.model.statistics.DelayCountEvent;
import com.alibaba.otter.shared.communication.model.statistics.DelayCountEvent.Action;
import com.alibaba.otter.shared.communication.model.statistics.TableStatEvent;
import com.alibaba.otter.shared.communication.model.statistics.ThroughputStatEvent;

/**
 * 统计信息的本地客户端服务
 * 
 * @author jianghang
 */
public class StatisticsClientServiceImpl implements StatisticsClientService, InitializingBean {

    private static final Logger                logger                = LoggerFactory.getLogger(StatisticsClientServiceImpl.class);
    private static final int                   DEFAULT_POOL          = 10;
    // 使用一个buffer队列，保证inc/desc/reset的发送操作为一个串行过程
    private BlockingQueue<DelayCountEvent>     delayCountStatsBuffer = new LinkedBlockingQueue<DelayCountEvent>(
                                                                                                                10 * 1000);
    private static ScheduledThreadPoolExecutor scheduler;
    private NodeCommmunicationClient           nodeCommmunicationClient;

    public void sendIncDelayCount(final DelayCount delayCount) {
        DelayCountEvent event = new DelayCountEvent();
        event.setCount(delayCount);
        event.setAction(Action.INC);

        boolean result = delayCountStatsBuffer.offer(event);
        if (result) {
            logger.info("add IncDelayCount to send with {}", delayCount);
        } else {
            logger.warn("add IncDelayCount failed by buffer is full with {}", delayCount);
        }
    }

    public void sendDecDelayCount(final DelayCount delayCount) {
        DelayCountEvent event = new DelayCountEvent();
        event.setCount(delayCount);
        event.setAction(Action.DEC);

        boolean result = delayCountStatsBuffer.offer(event);
        if (result) {
            logger.info("add sendDecDelayCount to send with {}", delayCount);
        } else {
            logger.warn("add sendDecDelayCount failed by buffer is full with {}", delayCount);
        }
    }

    public void sendResetDelayCount(final DelayCount delayCount) {
        DelayCountEvent event = new DelayCountEvent();
        event.setCount(delayCount);
        event.setAction(Action.RESET);

        boolean result = delayCountStatsBuffer.offer(event);
        if (result) {
            logger.info("add sendResetDelayCount to send with {}", delayCount);
        } else {
            logger.warn("add sendResetDelayCount failed by buffer is full with {}", delayCount);
        }
    }

    public void sendThroughputs(final List<ThroughputStat> stats) {
        ThroughputStatEvent event = new ThroughputStatEvent();
        event.setStats(stats);
        nodeCommmunicationClient.callManager(event, new Callback<Object>() {

            public void call(Object event) {
                logger.info("sendThroughput successed for {}", stats);
            }
        });

    }

    public void sendTableStats(final List<TableStat> stats) {
        TableStatEvent event = new TableStatEvent();
        event.setStats(stats);
        nodeCommmunicationClient.callManager(event, new Callback<Object>() {

            public void call(Object event) {
                logger.info("sendTableStats successed for {}", stats);
            }
        });
    }

    // ================= helper method ==============
    public void afterPropertiesSet() throws Exception {
        scheduler = new ScheduledThreadPoolExecutor(DEFAULT_POOL, new NamedThreadFactory("Otter-Statistics-Client"),
                                                    new ThreadPoolExecutor.CallerRunsPolicy());
        scheduler.submit(new Runnable() {

            public void run() {
                doSendDelayCountEvent();
            }
        });
    }

    private void doSendDelayCountEvent() {
        DelayCountEvent event = null;
        while (true) { // 尝试从队列里获取一下数据，不阻塞，没有就退出，等下个5秒再检查一次
            try {
                event = delayCountStatsBuffer.take();
                nodeCommmunicationClient.callManager(event);
                logger.info("sendDelayCountEvent successed for {}", event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return; // 退出
            } catch (Exception e) {
                LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS));
            }
        }
    }

    // ================ setter / getter =================

    public void setNodeCommmunicationClient(NodeCommmunicationClient nodeCommmunicationClient) {
        this.nodeCommmunicationClient = nodeCommmunicationClient;
    }

}
