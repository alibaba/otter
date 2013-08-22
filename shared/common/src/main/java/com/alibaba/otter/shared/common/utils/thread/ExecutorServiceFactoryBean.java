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

package com.alibaba.otter.shared.common.utils.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 基于spring 提供一个async的容器
 * 
 * @author jianghang 2011-11-14 下午03:48:57
 * @version 4.0.0
 */
public class ExecutorServiceFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

    private ThreadPoolExecutor executor;
    private static final int   DEFAULT_POOL_SIZE    = 50;
    private static final int   DEFAULT_ACCEPT_COUNT = 100;
    private int                poolSize             = DEFAULT_POOL_SIZE;
    private int                acceptCount          = DEFAULT_ACCEPT_COUNT;
    private String             name                 = "Otter-Async-Executor";

    public Object getObject() throws Exception {
        return executor;
    }

    public Class getObjectType() {
        return ThreadPoolExecutor.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        if (executor == null) {// 初始化一个默认值
            executor = new ThreadPoolExecutor(poolSize, poolSize, 60, TimeUnit.SECONDS,
                                              new ArrayBlockingQueue(acceptCount), new NamedThreadFactory(name),
                                              new ThreadPoolExecutor.CallerRunsPolicy());
        }
    }

    public void destroy() throws Exception {
        executor.shutdown();
    }

    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public void setAcceptCount(int acceptCount) {
        this.acceptCount = acceptCount;
    }

    public void setName(String name) {
        this.name = name;
    }

}
