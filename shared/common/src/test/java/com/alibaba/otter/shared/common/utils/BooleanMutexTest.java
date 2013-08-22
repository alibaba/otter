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

package com.alibaba.otter.shared.common.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.Test;

import com.alibaba.otter.shared.common.BaseOtterTest;
import com.alibaba.otter.shared.common.utils.lock.BooleanMutex;

/**
 * 锁对象测试
 * 
 * @author jianghang 2011-9-23 上午10:40:32
 * @version 4.0.0
 */
public class BooleanMutexTest extends BaseOtterTest {

    @Test
    public void test_init_true() {
        BooleanMutex mutex = new BooleanMutex(true);
        try {
            mutex.get(); // 不会被阻塞
        } catch (InterruptedException e) {
            want.fail();
        }
    }

    @Test
    public void test_init_false() {
        final BooleanMutex mutex = new BooleanMutex(false);
        try {
            final CountDownLatch count = new CountDownLatch(1);
            ExecutorService executor = Executors.newCachedThreadPool();

            executor.submit(new Callable() {

                public Object call() throws Exception {
                    Thread.sleep(1000);
                    mutex.set(true);
                    count.countDown();
                    return null;
                }
            });

            mutex.get(); // 会被阻塞，等异步线程释放锁对象
            count.await();
            executor.shutdown();
        } catch (InterruptedException e) {
            want.fail();
        }
    }

    @Test
    public void test_concurrent_true() {
        try {
            final BooleanMutex mutex = new BooleanMutex(true);
            final CountDownLatch count = new CountDownLatch(10);
            ExecutorService executor = Executors.newCachedThreadPool();

            for (int i = 0; i < 10; i++) {
                executor.submit(new Callable() {

                    public Object call() throws Exception {
                        mutex.get();
                        count.countDown();
                        return null;
                    }
                });
            }
            count.await();
            executor.shutdown();
        } catch (InterruptedException e) {
            want.fail();
        }
    }

    @Test
    public void test_concurrent_false() {
        try {
            final BooleanMutex mutex = new BooleanMutex(false);// 初始为false
            final CountDownLatch count = new CountDownLatch(10);
            ExecutorService executor = Executors.newCachedThreadPool();

            for (int i = 0; i < 10; i++) {
                executor.submit(new Callable() {

                    public Object call() throws Exception {
                        mutex.get();// 被阻塞
                        count.countDown();
                        return null;
                    }
                });
            }
            Thread.sleep(1000);
            mutex.set(true);
            count.await();
            executor.shutdown();
        } catch (InterruptedException e) {
            want.fail();
        }
    }
}
