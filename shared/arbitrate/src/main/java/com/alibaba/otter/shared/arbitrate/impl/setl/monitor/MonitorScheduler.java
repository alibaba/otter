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

package com.alibaba.otter.shared.arbitrate.impl.setl.monitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;

/**
 * monitor调度程序,定时调度{@linkplain Monitor}对象
 * 
 * @author jianghang 2011-9-19 下午08:55:42
 * @version 4.0.0
 */
public class MonitorScheduler {

    private static final Long                    DEFAULT_PERIOD = 60 * 1000L;
    private static final int                     DEFAULT_POOL   = 10;
    private static ScheduledThreadPoolExecutor   scheduler      = new ScheduledThreadPoolExecutor(
                                                                                                  DEFAULT_POOL,
                                                                                                  new NamedThreadFactory(
                                                                                                                         "Arbitrate-Monitor"),
                                                                                                  new ThreadPoolExecutor.CallerRunsPolicy());

    private static Map<Monitor, ScheduledFuture> register       = new ConcurrentHashMap<Monitor, ScheduledFuture>(10);

    /**
     * 注册对应的Monitor对象
     * 
     * @param monitor
     */
    public static void register(final Monitor monitor) {
        register(monitor, DEFAULT_PERIOD);
    }

    /**
     * 注册对应的Monitor对象
     * 
     * @param monitor
     */
    public static void register(final Monitor monitor, Long delay) {
        register(monitor, delay, DEFAULT_PERIOD);
    }

    /**
     * 注册对应的Monitor对象
     * 
     * @param monitor
     * @param period 调度周期，单位ms
     */
    public static void register(final Monitor monitor, Long delay, Long period) {
        ScheduledFuture future = scheduler.scheduleAtFixedRate(new Runnable() {

            public void run() {
                monitor.reload();
            }
        }, delay, period, TimeUnit.MILLISECONDS);

        register.put(monitor, future);
    }

    /**
     * 取消注册对应的Monitor对象
     * 
     * @param monitor
     */
    public static void unRegister(Monitor monitor) {
        ScheduledFuture future = register.remove(monitor);
        if (future != null) {
            future.cancel(true);// 打断
        }
    }
}
