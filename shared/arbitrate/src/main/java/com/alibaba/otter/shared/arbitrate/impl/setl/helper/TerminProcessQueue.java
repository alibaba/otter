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

package com.alibaba.otter.shared.arbitrate.impl.setl.helper;

import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 结束信号对应的queue模型, peek/ack组合
 * 
 * @author jianghang 2012-7-3 下午03:10:43
 * @version 4.1.0
 */
public class TerminProcessQueue {

    private PriorityQueue<Long>            queue    = new PriorityQueue<Long>();
    private static final Object            PRESENT  = new Object();
    private LRULinkedHashMap<Long, Object> history;                             // 记录一下最近分配出去的processId，容量必须>当前并行度
    private ReentrantLock                  lock     = new ReentrantLock();
    private Condition                      notEmpty = lock.newCondition();

    public TerminProcessQueue(){
        history = new LRULinkedHashMap<Long, Object>(100);
    }

    /**
     * 从queue中获取第一个数据节点，不移除，等待ack信号进行移除
     * 
     * @return
     * @throws InterruptedException
     */
    public Long peek() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (queue.size() == 0) {
                notEmpty.await();
            }

            return queue.peek();
        } finally {
            lock.unlock();
        }
    }

    public boolean offer(Long processId) {
        lock.lock();
        try {
            if (contains(processId)) {
                return false;
            }

            int size = queue.size();
            queue.add(processId);
            if (size == 0) {
                notEmpty.signalAll();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(Long processId) {
        return queue.contains(processId) || history.containsKey(processId);
    }

    /**
     * 物理移除queue中第一个数据节点，不移除
     * 
     * @return
     */
    public boolean ack() {
        lock.lock();
        try {
            Long result = queue.poll();
            if (result != null) {// 添加到历史记录里，避免重复
                history.put(result, PRESENT);
            }
            return result != null;
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            queue.clear();
            history.clear();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}
