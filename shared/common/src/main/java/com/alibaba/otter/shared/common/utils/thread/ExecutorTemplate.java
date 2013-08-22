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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 多线程执行器模板代码，otter中好多地方都写多线程，比较多的都是重复的逻辑代码，抽象一下做个模板把
 * 
 * @author jianghang 2013-2-26 下午10:46:43
 * @version 4.1.7
 */
public class ExecutorTemplate implements InitializingBean, DisposableBean {

    private static final Logger                logger            = LoggerFactory.getLogger(ExecutorTemplate.class);
    private String                             name              = "ExecutorTemplate";
    private static final int                   DEFAULT_POOL_SIZE = 5;
    private int                                poolSize          = DEFAULT_POOL_SIZE;
    private ExecutorService                    executor;

    private volatile ExecutorCompletionService completionService = null;
    private volatile List<Future>              futures           = null;

    public void start() {
        completionService = new ExecutorCompletionService(executor);
        futures = Collections.synchronizedList(new ArrayList<Future>());
    }

    public void submit(Callable<Exception> task) {
        Future future = completionService.submit(task);
        futures.add(future);
        check(future);// 立即check下，fast-fail
    }

    public void submit(Runnable task) {
        Future future = completionService.submit(task, null);
        futures.add(future);
        check(future);// 立即check下，fast-fail
    }

    private void check(Future future) {
        if (future.isDone()) {
            // 立即判断一次，因为使用了CallerRun可能当场跑出结果，针对有异常时快速响应，而不是等跑完所有的才抛异常
            try {
                future.get();
            } catch (InterruptedException e) {
                cancel();// 取消完之后立马退出
                throw new RuntimeException(e);
            } catch (Throwable e) {
                cancel(); // 取消完之后立马退出
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized List<?> waitForResult() {
        List result = new ArrayList();
        RuntimeException exception = null;
        // 开始处理结果
        int index = 0;
        while (index < futures.size()) { // 循环处理发出去的所有任务
            try {
                Future future = completionService.take();// 它也可能被打断
                result.add(future.get());
            } catch (InterruptedException e) {
                exception = new RuntimeException(e);
                break;// 如何一个future出现了异常，就退出
            } catch (Throwable e) {
                exception = new RuntimeException(e);
                break;// 如何一个future出现了异常，就退出
            }

            index++;
        }

        if (index < futures.size()) {
            // 小于代表有错误，需要对未完成的记录进行cancel操作，对已完成的结果进行收集，做重复录入过滤记录
            cancel();
            throw exception;
        } else {
            return result;
        }
    }

    public void cancel() {
        logger.info("canal Futures[{}]", futures.size());
        for (int i = 0; i < futures.size(); i++) {
            Future future = futures.get(i);
            if (!future.isDone() && !future.isCancelled()) {
                future.cancel(true);
            }
        }
    }

    // 调整一下线程池
    public void adjustPoolSize(int newPoolSize) {
        if (newPoolSize != poolSize) {
            poolSize = newPoolSize;
            if (executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
                pool.setCorePoolSize(newPoolSize);
                pool.setMaximumPoolSize(newPoolSize);
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        executor = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                                          new ArrayBlockingQueue(poolSize * 4), new NamedThreadFactory(name),
                                          new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void destroy() throws Exception {
        if (futures != null) {
            futures.clear();
        }

        executor.shutdownNow(); // 立即关闭
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public void setName(String name) {
        this.name = name;
    }

}
