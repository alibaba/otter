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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;

/**
 * 包装ZooKeeper的Watcher接口，支持Async的异步调用处理
 * 
 * <pre>
 * 说明：
 *  1. zookeeper针对watcher的调用是以单线程串行的方式进行处理，容易造成堵塞影响，monitor的数据同步及时性
 *  2. AsyncWatcher为采取的一种策略为当不超过acceptCount=60的任务时，会采用异步线程的方式处理。如果超过60任务，会变为原先的单线程串行的模式
 * </pre>
 * 
 * @author jianghang 2011-9-21 下午01:00:39
 * @version 4.0.0
 */
public abstract class AsyncWatcher implements Watcher {

    private static final int       DEFAULT_POOL_SIZE    = 30;
    private static final int       DEFAULT_ACCEPT_COUNT = 60;

    private static ExecutorService executor             = new ThreadPoolExecutor(
                                                                                 DEFAULT_POOL_SIZE,
                                                                                 DEFAULT_POOL_SIZE,
                                                                                 0L,
                                                                                 TimeUnit.MILLISECONDS,
                                                                                 new ArrayBlockingQueue(
                                                                                                        DEFAULT_ACCEPT_COUNT),
                                                                                 new NamedThreadFactory(
                                                                                                        "Arbitrate-Async-Watcher"),
                                                                                 new ThreadPoolExecutor.CallerRunsPolicy());

    public void process(final WatchedEvent event) {
        executor.execute(new Runnable() {// 提交异步处理

            public void run() {
                asyncProcess(event);
            }
        });

    }

    public abstract void asyncProcess(WatchedEvent event);

}
