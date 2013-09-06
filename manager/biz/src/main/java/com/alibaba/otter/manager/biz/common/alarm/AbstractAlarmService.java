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

package com.alibaba.otter.manager.biz.common.alarm;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 报警服务实现
 * 
 * @author jianghang 2011-11-3 上午11:12:16
 * @version 4.0.0
 */
public abstract class AbstractAlarmService implements AlarmService, InitializingBean, DisposableBean {

    private static final Logger         logger = LoggerFactory.getLogger(AbstractAlarmService.class);

    private BlockingQueue<AlarmMessage> queue  = new LinkedBlockingQueue<AlarmMessage>(3 * 3 * 3600);
    private ExecutorService             executor;
    private int                         period = 150;                                                // milliseconds

    public void sendAlarm(AlarmMessage data) {
        try {
            if (!queue.offer(data, 2, TimeUnit.SECONDS)) {
                logger.error(String.format("alarm sent to queue error : [%s]", data.toString()));
            }
        } catch (Exception e) {
            logger.error(String.format("send alarm [%s] to drgoon agent error!", data.toString()), e);
        }
    }

    private void sendAlarmInternal() {
        AlarmMessage data = null;
        try {
            data = queue.take();
            doSend(data);
            logger.info(String.format("has sent alarm [%s] to drgoon agent.", data.toString()));
        } catch (InterruptedException e) {
            logger.warn("otter-sendAlarm-worker was interrupted", e);
        } catch (Exception e) {
            logger.error(String.format("send alarm [%s] to drgoon agent error!", data.toString()), e);
        }
    }

    protected abstract void doSend(AlarmMessage data) throws Exception;

    public void afterPropertiesSet() throws Exception {
        executor = Executors.newFixedThreadPool(1);
        executor.submit(new Runnable() {

            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    sendAlarmInternal();
                    LockSupport.parkNanos(period * 1000L * 1000L);
                }
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
        if (!queue.isEmpty()) {
            int size = queue.size();
            logger.warn(String.format("there are %d alarms wait to be sent \n %s", size, dumpQueue()));
        }
    }

    protected String dumpQueue() {
        if (queue.isEmpty()) {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (AlarmMessage data : queue) {
            sb.append(data.toString()).append("\n");
        }

        return sb.toString();
    }

    // ============= setter ===============

    public void setPeriod(int period) {
        this.period = period;
    }

}
