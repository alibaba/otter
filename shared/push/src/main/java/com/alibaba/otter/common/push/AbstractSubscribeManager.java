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

package com.alibaba.otter.common.push;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * 帮助子类管理callback的manager
 * 
 * @author zebin.xuzb 2012-9-19 下午4:10:55
 * @version 4.1.0
 */
public abstract class AbstractSubscribeManager implements SubscribeManager {

    private boolean                                inited      = false;
    private ConcurrentMap<String, Object>          mutexes     = new ConcurrentHashMap<String, Object>();

    private SetMultimap<String, SubscribeCallback> callBackMap = HashMultimap.create();

    @Override
    public synchronized void init() {
        if (inited) {
            return;
        }
        doInit();
        inited = true;
    }

    protected abstract void doInit();

    @Override
    public synchronized void shutdown() {
        if (!inited) {
            return;
        }
        doShutdown();
        inited = false;
    }

    protected abstract void doShutdown();

    @Override
    public void registerCallback(String dataId, SubscribeCallback callback) {
        registerCallback(dataId, null, callback);
    }

    @Override
    public void registerCallback(String dataId, String groupId, SubscribeCallback callback) {
        String key = generateKey(dataId, groupId);
        doRegisterCallback(dataId, groupId, callback, key);
    }

    private void doRegisterCallback(String dataId, String groupId, SubscribeCallback callback, String key) {
        Object lock = getLock(dataId, groupId);
        synchronized (lock) {
            callBackMap.put(key, callback);
            postRegisterCallback(dataId, groupId, callback);
        }
    }

    @Override
    public void unRegisterCallback(String dataId, SubscribeCallback callback) {
        unRegisterCallback(dataId, null, callback);
    }

    @Override
    public void unRegisterCallback(String dataId, String groupId, SubscribeCallback callback) {
        String key = generateKey(dataId, groupId);
        doUnregister(dataId, groupId, callback, key);
    }

    private void doUnregister(String dataId, String groupId, SubscribeCallback callback, String key) {
        Object lock = getLock(key);
        synchronized (lock) {
            Set<SubscribeCallback> callbacks = callBackMap.get(key);
            boolean effectRemoved = callbacks.remove(callback);
            if (!effectRemoved) {
                return;
            }
            if (callbacks.isEmpty()) {
                doWhenCallbackEmpty(dataId, groupId, callback);
            }

        }
    }

    /**
     * 当同一个 dataId-groupId 的callback 被清楚完成之后的动作。同于同一个 dataId-groupId，此方法不会并发
     * 
     * @param dataId
     * @param groupId
     * @param callback
     */
    protected void doWhenCallbackEmpty(String dataId, String groupId, SubscribeCallback callback) {
        // for subclass to close some resources
    }

    /**
     * 注册监听器之后的方法，同于同一个 dataId-groupId，此方法不会并发
     * 
     * @param dataId
     * @param groupId
     * @param callback
     */
    protected void postRegisterCallback(String dataId, String groupId, SubscribeCallback callback) {
        // for subclass to extend
    }

    protected Set<SubscribeCallback> getCallbacks(String dataId) {
        return getCallbacks(dataId, null);
    }

    protected Set<SubscribeCallback> getCallbacks(String dataId, String groupId) {
        String key = generateKey(dataId, groupId);
        return callBackMap.get(key);
    }

    protected static String generateKey(String dataId, String groupId) {
        return StringUtils.trimToEmpty(dataId) + "_" + StringUtils.trimToEmpty(groupId);
    }

    protected Object getLock(String dataId, String groupId) {
        String key = generateKey(dataId, groupId);
        return getLock(key);
    }

    protected Object getLock(String key) {
        Object lock = new Object();
        Object expectLock = mutexes.putIfAbsent(key, lock);
        if (expectLock == null) {
            expectLock = lock;
        }
        return expectLock;
    }

}
