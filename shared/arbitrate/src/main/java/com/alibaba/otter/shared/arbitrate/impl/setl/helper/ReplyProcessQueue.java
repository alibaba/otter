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

import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 自定义queue实现，简单的合并history + list的功能
 * 
 * <pre>
 * 修改记录：
 * 1. 2012-09-08 by ljh
 *  将LinkedList换成了PriorityQueue进行存储，保证并发处理时，保证processId小的一定会得到优先处理。
 *  a. 避免在s模块出现processId大的先被分了出去，导致加载顺序会出错
 *  b. 尽可能的优先处理processId小的，因为只有之前的processId的s/e/t/l都处理完了，下一个processId才会进行load
 * </pre>
 * 
 * @author jianghang 2012-6-28 上午10:12:25
 * @version 4.1.0
 */
public class ReplyProcessQueue {

    private static final Object            PRESENT  = new Object();
    private LRULinkedHashMap<Long, Object> history;                             // 记录一下最近分配出去的processId，容量必须>当前并行度
    private PriorityQueue<Long>            tables   = new PriorityQueue<Long>();
    private ReentrantLock                  lock     = new ReentrantLock();
    private Condition                      notEmpty = lock.newCondition();

    public ReplyProcessQueue(int historySize){
        history = new LRULinkedHashMap<Long, Object>(historySize);
    }

    public Long take() throws InterruptedException {
        try {
            lock.lockInterruptibly();
            Long result = null;
            do {
                if (tables.size() == 0) {
                    notEmpty.await();
                }

                result = tables.poll();
            } while (result == null);

            history.put(result, PRESENT);
            return result;
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

            int size = tables.size();
            // tables.addLast(processId);
            tables.add(processId);// 添加记录
            if (size == 0) {
                notEmpty.signalAll();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(Long processId) {
        lock.lock();
        try {
            boolean result = tables.remove(processId);
            if (result) {
                history.put(processId, PRESENT); // 记录一下到历史记录
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            tables.clear();
            history.clear();
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(Long processId) {
        return tables.contains(processId) || history.containsKey(processId);
    }

    public Object[] toArray() {
        return tables.toArray();
    }

    public int size() {
        return tables.size();
    }

}

/**
 * 简单的继承实现LRU算法对象,注意需要控制多线程
 * 
 * @author jianghang 2011-9-28 上午10:18:06
 * @version 4.0.0
 */
class LRULinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    private static final long  serialVersionUID    = 1827912970480911024L;

    private final int          maxCapacity;

    private static final float DEFAULT_LOAD_FACTOR = 1f;

    public LRULinkedHashMap(int maxCapacity){
        super(maxCapacity, DEFAULT_LOAD_FACTOR, false);
        this.maxCapacity = maxCapacity;
    }

    protected boolean removeEldestEntry(Entry eldest) {
        return (size() > this.maxCapacity);
    }
}
