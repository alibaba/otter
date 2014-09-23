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

package com.alibaba.otter.node.etl.load.loader.weight;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 构建基于weight的barrier控制
 * 
 * <pre>
 * 场景：
 *   多个loader模块会进行并行加载，但每个loader的加载数据的进度统一受到weight的调度，只有当前的weight的所有数据都完成后，不同loader中的下一个weight才允许开始
 * 
 * 实现：
 * 1. 使用AQS构建了一个基于weight的barrier处理，使用一个state进行控制(代表当前运行<state以下的weight运行)，
 * 2. 多个任务之间通过single(weight)进行协调控制
 * </pre>
 * 
 * @author jianghang 2011-11-1 上午11:24:56
 * @version 4.0.0
 */
public class WeightBarrier {

    private ReentrantLock lock      = new ReentrantLock();
    private Condition     condition = lock.newCondition();
    private volatile long threshold;

    public WeightBarrier(){
        this(Long.MAX_VALUE);
    }

    public WeightBarrier(long weight){
        this.threshold = weight;
    }

    /**
     * 阻塞等待weight允许执行
     * 
     * <pre>
     * 阻塞返回条件：
     *  1. 中断事件
     *  2. 其他线程single()的weight > 当前阻塞等待的weight
     * </pre>
     * 
     * @throws InterruptedException
     */
    public void await(long weight) throws InterruptedException {
        try {
            lock.lockInterruptibly();
            while (isPermit(weight) == false) {
                condition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 阻塞等待当前的weight处理,允许设置超时时间
     * 
     * <pre>
     * 阻塞返回条件：
     *  1. 中断事件
     *  2. 其他线程single()的weight > 当前阻塞等待的weight
     *  3. 超时
     * </pre>
     * 
     * @param timeout
     * @param unit
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public void await(long weight, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        try {
            lock.lockInterruptibly();
            while (isPermit(weight) == false) {
                condition.await(timeout, unit);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 重新设置weight信息
     * 
     * @throws InterruptedException
     */
    public void single(long weight) throws InterruptedException {
        try {
            lock.lockInterruptibly();
            threshold = weight;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public long state() {
        return threshold;
    }

    private boolean isPermit(long state) {
        return state <= state();
    }
}
