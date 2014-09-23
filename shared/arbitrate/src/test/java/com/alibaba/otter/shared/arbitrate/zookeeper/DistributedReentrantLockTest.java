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

package com.alibaba.otter.shared.arbitrate.zookeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.zookeeper.KeeperException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.lock.DistributedReentrantLock;

/**
 * @author jianghang 2011-9-29 下午02:36:19
 * @version 4.0.0
 */
public class DistributedReentrantLockTest extends BaseEventTest {

    private final String dir = "/" + getClass().getSimpleName();

    @BeforeClass
    public void init() {
        getZookeeper();
    }

    @Test
    protected void test_lock() {
        ExecutorService exeucotr = Executors.newCachedThreadPool();
        final int count = 50;
        final CountDownLatch latch = new CountDownLatch(count);

        final DistributedReentrantLock lock = new DistributedReentrantLock(dir);
        for (int i = 0; i < count; i++) {
            exeucotr.submit(new Runnable() {

                public void run() {
                    try {
                        Thread.sleep(1000);
                        lock.lock();
                        Thread.sleep(100 + RandomUtils.nextInt(100));

                        System.out.println("id: " + lock.getId() + " is leader: " + lock.isOwner());
                    } catch (InterruptedException e) {
                        want.fail();
                    } catch (KeeperException e) {
                        want.fail();
                    } finally {
                        latch.countDown();
                        try {
                            lock.unlock();
                        } catch (KeeperException e) {
                            want.fail();
                        }
                    }

                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            want.fail();
        }

        exeucotr.shutdown();
    }

    @Test
    protected void test_try_lock() {
        ExecutorService exeucotr = Executors.newCachedThreadPool();
        final int count = 50;
        final CountDownLatch latch = new CountDownLatch(count);

        final DistributedReentrantLock lock = new DistributedReentrantLock(dir);
        for (int i = 0; i < count; i++) {
            exeucotr.submit(new Runnable() {

                public void run() {
                    try {
                        while (lock.tryLock() == false) {
                            Thread.sleep(100 + RandomUtils.nextInt(100));
                        }

                        System.out.println("id: " + lock.getId() + " is leader: " + lock.isOwner());
                    } catch (InterruptedException e) {
                        want.fail();
                    } catch (KeeperException e) {
                        want.fail();
                    } finally {
                        latch.countDown();
                        try {
                            lock.unlock();
                        } catch (KeeperException e) {
                            want.fail();
                        }
                    }

                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            want.fail();
        }

        exeucotr.shutdown();
    }
}
