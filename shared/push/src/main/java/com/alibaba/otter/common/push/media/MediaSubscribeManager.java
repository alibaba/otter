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

package com.alibaba.otter.common.push.media;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.common.push.AbstractSubscribeManager;
import com.alibaba.otter.common.push.SubscribeCallback;
import com.alibaba.otter.shared.arbitrate.impl.communication.ArbitrateCommmunicationClient;
import com.alibaba.otter.shared.common.model.config.ConfigException;
import com.alibaba.otter.shared.common.utils.cache.RefreshMemoryMirror;
import com.alibaba.otter.shared.common.utils.cache.RefreshMemoryMirror.ComputeFunction;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.alibaba.otter.shared.communication.model.config.FindMediaEvent;

/**
 * 基于otter manager media的管理
 * 
 * @author jianghang 2013-4-18 下午12:11:53
 * @version 4.1.8
 */
public class MediaSubscribeManager extends AbstractSubscribeManager {

    private static final Long                   DEFAULT_PERIOD = 60 * 1000L;
    private static final Logger                 logger         = LoggerFactory.getLogger(MediaSubscribeManager.class);
    private ConcurrentMap<String, Object>       mutexes        = new ConcurrentHashMap<String, Object>();
    private Map<String, Runnable>               runnableMap    = new ConcurrentHashMap<String, Runnable>();
    private Long                                timeout        = DEFAULT_PERIOD;
    private RefreshMemoryMirror<String, String> matrixCache;
    private ArbitrateCommmunicationClient       arbitrateCommmunicationClient;

    private int                                 poolSize       = 8;
    private ScheduledThreadPoolExecutor         executor;

    public MediaSubscribeManager(){
        // 注册一下事件处理
        ComputeFunction function = new ComputeFunction<String, String>() {

            public String apply(final String key, String oldValue) {
                FindMediaEvent event = new FindMediaEvent();
                event.setDataId(key);
                try {
                    Object obj = arbitrateCommmunicationClient.callManager(event);
                    if (obj != null && obj instanceof String) {
                        final String value = (String) obj;
                        if (!StringUtils.equalsIgnoreCase(oldValue, value)) {
                            // 触发一下变化
                            executor.submit(new Runnable() {

                                public void run() {
                                    Set<SubscribeCallback> callbacks = getCallbacks(key, null);
                                    for (SubscribeCallback callback : callbacks) {
                                        callback.callback(value);
                                    }
                                }
                            });
                        }
                        return value;
                    } else {
                        throw new ConfigException("No Such dataId[" + key + "]");
                    }
                } catch (Exception e) {
                    logger.error("call_manager_error", event.toString(), e);
                }
                // 其他情况直接返回内存中的旧值
                return oldValue;
            }

        };
        matrixCache = new RefreshMemoryMirror<String, String>(timeout, function);
    }

    protected void doInit() {
        if (executor != null) {
            return;
        }

        executor = new ScheduledThreadPoolExecutor(poolSize, new NamedThreadFactory("canal-media-callback-worker"),
                                                   new ThreadPoolExecutor.CallerRunsPolicy());
    }

    protected void doShutdown() {
        if (executor != null) {
            return;
        }
        executor.shutdown();
    }

    public String fetchConfig(String dataId) {
        return fetchConfig(dataId, 0);
    }

    public String fetchConfig(String dataId, long timeout) {
        return matrixCache.get(dataId);
    }

    public String fetchConfig(String dataId, String groupId) {
        return fetchConfig(dataId);
    }

    public String fetchConfig(String dataId, String groupId, long timeout) {
        return fetchConfig(dataId, 0);
    }

    protected void postRegisterCallback(String dataId, String groupId, SubscribeCallback callback) {
        String key = generateKey(dataId, groupId);
        Object lock = new Object();

        // 原子性的获取一个锁
        lock = mutexes.putIfAbsent(key, lock);
        if (lock == null) {
            lock = mutexes.get(key);

            synchronized (lock) {
                submitSchedule(dataId, groupId);
            }
        }
    }

    protected void doWhenCallbackEmpty(String dataId, String groupId, SubscribeCallback callback) {
        String key = generateKey(dataId, groupId);
        Object lock = new Object();

        // 原子性的获取一个锁
        lock = mutexes.putIfAbsent(key, lock);
        if (lock == null) {
            lock = mutexes.get(key);
        }

        synchronized (lock) {
            colseSchedule(dataId, groupId);
            matrixCache.remove(dataId);
        }
    }

    private void submitSchedule(final String dataId, final String groupId) {
        String key = generateKey(dataId, groupId);
        Runnable runnable = runnableMap.get(key);
        if (runnable == null) {
            runnable = new Runnable() {

                public void run() {
                    try {
                        matrixCache.get(dataId);
                    } catch (Throwable e) {
                        logger.error("reload failed", e);
                    }
                }
            };

            runnableMap.put(key, runnable);
            executor.scheduleAtFixedRate(runnable, timeout, timeout, TimeUnit.MILLISECONDS);
        }
    }

    private void colseSchedule(final String dataId, final String groupId) {
        String key = generateKey(dataId, groupId);
        Runnable runnable = runnableMap.remove(key);
        if (runnable != null) {
            executor.remove(runnable);
        }
    }

    public void setArbitrateCommmunicationClient(ArbitrateCommmunicationClient arbitrateCommmunicationClient) {
        this.arbitrateCommmunicationClient = arbitrateCommmunicationClient;
    }

}
