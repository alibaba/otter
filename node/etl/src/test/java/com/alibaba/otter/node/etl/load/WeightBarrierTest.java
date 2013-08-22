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

package com.alibaba.otter.node.etl.load;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.BaseOtterTest;
import com.alibaba.otter.node.etl.load.loader.weight.WeightBarrier;

public class WeightBarrierTest extends BaseOtterTest {

    @Test
    public void test_simple() {
        final WeightBarrier barrier = new WeightBarrier(10);
        try {
            barrier.await(10);// 可以成功通过
        } catch (InterruptedException e1) {
            want.fail();
        }

        try {
            final CountDownLatch count = new CountDownLatch(1);
            ExecutorService executor = Executors.newCachedThreadPool();

            executor.submit(new Callable() {

                public Object call() throws Exception {
                    Thread.sleep(1000);
                    barrier.single(11);
                    count.countDown();
                    return null;
                }
            });

            barrier.await(11);// 会被阻塞
            count.await();
            executor.shutdown();
        } catch (InterruptedException e) {
            want.fail();
        }
    }

    @Test
    public void test_cocurrent() {
        final WeightBarrier barrier = new WeightBarrier(-1);

        try {
            final CountDownLatch count = new CountDownLatch(10);
            ExecutorService executor = Executors.newCachedThreadPool();
            for (int i = 0; i < 10; i++) {
                final long index = i;
                executor.submit(new Callable() {

                    public Object call() throws Exception {
                        barrier.await(index);
                        want.number(index).isLe(barrier.state());
                        count.countDown();
                        return null;
                    }
                });
            }
            Thread.sleep(1000);
            for (int i = 0; i < 10; i++) {
                barrier.single(i);
            }
            count.await();
            executor.shutdown();
        } catch (InterruptedException e) {
            want.fail();
        }
    }
}
