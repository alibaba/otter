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

/**
 * @author zebin.xuzb 2013-1-23 下午2:01:06
 * @since 4.1.3
 */
public interface SubscribeManager {

    void init();

    void shutdown();

    /**
     * 注册监听dataId的callback，如果对应dataId的配置发生了变化，则回调callback。<br/>
     * callback 的处理逻辑应该尽可能的快，如果callback处理的时间需要很长，建议采取异步的方式进行处理 <br/>
     * 或者实现可以起一个线程池，帮助客户端吧callback做异步处理。
     * 
     * @param dataId
     * @param callback
     */
    void registerCallback(String dataId, SubscribeCallback callback);

    /**
     * 移除监听事件
     * 
     * @param dataId
     * @param callback
     */
    void unRegisterCallback(String dataId, SubscribeCallback callback);

    /**
     * 同 {@linkplain SubscribeManager#registerCallback}。<br/>
     * groupId 也是组成配置key的部分，可以为<code>null</code>。
     * 
     * @param dataId
     * @param groupId
     * @param callback
     */
    void registerCallback(String dataId, String groupId, SubscribeCallback callback);

    /**
     * 移除监听事件
     * 
     * @param dataId
     * @param groupId
     * @param callback
     */
    void unRegisterCallback(String dataId, String groupId, SubscribeCallback callback);

    /**
     * 主动获取配置信息
     * 
     * @param dataId
     * @return 配置信息
     */
    String fetchConfig(String dataId);

    /**
     * 主动获取配置信息
     * 
     * @param dataId
     * @param timeout 超时时间，非正数代表无限长，单位毫秒
     * @return 配置信息
     */
    String fetchConfig(String dataId, long timeout);

    /**
     * 同 {@linkplain SubscribeManager#fetchConfig(String)}。<br/>
     * groupId 也是组成配置key的部分，可以为<code>null</code>。
     * 
     * @param dataId
     * @param groupId
     * @return 配置信息
     */
    String fetchConfig(String dataId, String groupId);

    /**
     * 同 {@linkplain SubscribeManager#fetchConfig(String)}。<br/>
     * groupId 也是组成配置key的部分，可以为<code>null</code>。
     * 
     * @param dataId
     * @param groupId
     * @param timeout 超时时间，非正数代表无限长，单位毫秒
     * @return 配置信息
     */
    String fetchConfig(String dataId, String groupId, long timeout);
}
