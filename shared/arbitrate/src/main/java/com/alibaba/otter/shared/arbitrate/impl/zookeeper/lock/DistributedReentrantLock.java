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

package com.alibaba.otter.shared.arbitrate.impl.zookeeper.lock;

import java.text.MessageFormat;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.KeeperException;

/**
 * 基于{@linkplain ReentrantLock}和{@linkplain DistributedLock} 的功能组合，实现多进程+多线程全方位的lock控制
 * 
 * @author jianghang 2011-9-30 上午09:48:05
 * @version 4.0.0
 */
public class DistributedReentrantLock extends DistributedLock {

    private static final String ID_FORMAT     = "Thread[{0}] Distributed[{1}]";
    private ReentrantLock       reentrantLock = new ReentrantLock();

    public DistributedReentrantLock(String root){
        super(root);
    }

    public void lock() throws InterruptedException, KeeperException {
        reentrantLock.lock();//多线程竞争时，先拿到第一层锁
        super.lock();
    }

    public boolean tryLock() throws KeeperException {
        //多线程竞争时，先拿到第一层锁
        return reentrantLock.tryLock() && super.tryLock();
    }

    public void unlock() throws KeeperException {
        super.unlock();
        reentrantLock.unlock();//多线程竞争时，释放最外层锁
    }

    @Override
    public String getId() {
        return MessageFormat.format(ID_FORMAT, Thread.currentThread().getId(), super.getId());
    }

    @Override
    public boolean isOwner() {
        return reentrantLock.isHeldByCurrentThread() && super.isOwner();
    }

}
