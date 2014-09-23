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

package com.alibaba.otter.shared.arbitrate.impl.zookeeper;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;

/**
 * zookeeper的心跳机制，保证和zookeeper的连接有请求
 * 
 * <pre>
 * 1. 尽量避免SessionExpired异常
 * 2. 出现SessionExpired/ConnectLoss异常时，能触发重连和链接恢复
 * 
 * <pre>
 * @author jianghang 2012-8-28 下午10:30:33
 * @version 4.1.0
 */
public class ZooKeeperHeartBeatWorker implements InitializingBean, DisposableBean {

    private static final Logger         logger    = LoggerFactory.getLogger(ZooKeeperHeartBeatWorker.class);
    private int                         period    = 3000;
    private ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(
                                                                                    1,
                                                                                    new NamedThreadFactory(
                                                                                                           "Otter-zookeeper-heartbeat"),
                                                                                    new ThreadPoolExecutor.CallerRunsPolicy());
    private NodeArbitrateEvent          nodeEvent;

    public void start() {
        scheduler.scheduleAtFixedRate(new Runnable() {

            public void run() {
                try {
                    nodeEvent.liveNodes();
                } catch (Throwable e) {
                    logger.warn("zookeeper heartbeat has failed.", e);
                }
            }
        }, period, period, TimeUnit.MILLISECONDS);

    }

    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void destroy() throws Exception {
        scheduler.shutdownNow();
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setNodeEvent(NodeArbitrateEvent nodeEvent) {
        this.nodeEvent = nodeEvent;
    }

}
