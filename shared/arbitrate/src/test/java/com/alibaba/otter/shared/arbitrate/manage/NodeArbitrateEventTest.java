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

package com.alibaba.otter.shared.arbitrate.manage;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 测试下Node manage
 * 
 * @author jianghang 2011-9-19 上午11:37:12
 * @version 4.0.0
 */
public class NodeArbitrateEventTest extends BaseEventTest {

    private ZkClientx          zookeeper = null;
    private NodeArbitrateEvent nodeEvent;

    @BeforeMethod
    public void setUp() {
        zookeeper = getZookeeper();
        nodeEvent = new NodeArbitrateEvent();
    }

    @Test
    public void testDestory() {
        nodeEvent.init(100L);
        nodeEvent.destory(100L);

        String path = StagePathUtils.getNode(100L);
        want.bool(zookeeper.exists(path)).is(false);
    }

    // @Test
    // public void testInit_immediate() {
    // nodeEvent.init(1L);
    //
    // final CountDownLatch count = new CountDownLatch(1);
    // ExecutorService executor = Executors.newCachedThreadPool();
    //
    // nodeEvent.setZookeeper(getZookeeper());// 创建新的链接
    // executor.submit(new Callable() {
    //
    // public Object call() throws Exception {
    // nodeEvent.awaitForNotExist(1L);
    // count.countDown();
    // return null;
    // }
    // });
    //
    // try {
    // zookeeper.close();//老链接进行关闭, awaitForNotExist就会被唤醒
    // } catch (InterruptedException e) {
    // want.fail();
    // }
    //
    // try {
    // count.await();
    // executor.shutdown();
    // } catch (InterruptedException e) {
    // want.fail();
    // }
    //
    // }
    //
    // @Test
    // public void testInit_sleep() {
    // nodeEvent.init(1L);
    //
    // final CountDownLatch count = new CountDownLatch(1);
    // ExecutorService executor = Executors.newCachedThreadPool();
    //
    // nodeEvent.setZookeeper(getZookeeper());// 创建新的链接
    // executor.submit(new Callable() {
    //
    // public Object call() throws Exception {
    // nodeEvent.awaitForNotExist(1L);
    // System.out.println("node close");
    // count.countDown();
    // return null;
    // }
    // });
    //
    // try {
    // Thread.sleep(1000); // sleep一下，等待awaitForNotExist进入watcher事件
    // } catch (InterruptedException e) {
    // want.fail();
    // }
    //
    // try {
    // zookeeper.close();//老链接进行关闭, awaitForNotExist就会被唤醒
    // } catch (InterruptedException e) {
    // want.fail();
    // }
    //
    // try {
    // count.await();
    // } catch (InterruptedException e) {
    // want.fail();
    // }
    //
    // }
}
