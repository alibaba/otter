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

package com.alibaba.otter.shared.arbitrate.setl.monitor.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.SelectStageListener;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.StageMonitor;
import com.alibaba.otter.shared.arbitrate.setl.BaseStageTest;

/**
 * 测试下select模块
 * 
 * @author jianghang 2011-9-21 下午08:18:10
 * @version 4.0.0
 */
public class SelectStageListenerTest extends BaseStageTest {

    @Test
    public void testProcess_init() {
        final List<Long> initProcessIds = new ArrayList<Long>();
        try {
            initProcess();
            // initProcessIds.add(p1);
            SelectStageListener select = new SelectStageListener(pipelineId);
            sleep();

            Long p2 = select.waitForProcess();
            Long p3 = select.waitForProcess();
            Long p4 = select.waitForProcess();
            initProcessIds.add(p2);
            initProcessIds.add(p3);
            initProcessIds.add(p4);

            StageMonitor monitor = ArbitrateFactory.getInstance(pipelineId, StageMonitor.class);
            List<Long> processIds = monitor.getCurrentProcessIds();

            want.collection(processIds).isEqualTo(initProcessIds);
            select.destory();
            ArbitrateFactory.destory(pipelineId);
        } catch (InterruptedException e) {
            want.fail();
        } finally {
            for (Long processId : initProcessIds) {
                destoryProcess(processId);
            }

        }
    }

    @Test
    public void testProcess_dymanic() {
        final List<Long> initProcessIds = new ArrayList<Long>();
        try {
            initProcess();
            SelectStageListener select = new SelectStageListener(pipelineId);
            final Long p2 = select.waitForProcess();
            final Long p3 = select.waitForProcess();

            final CountDownLatch count = new CountDownLatch(1);
            ExecutorService executor = Executors.newCachedThreadPool();
            executor.submit(new Runnable() {

                public void run() {
                    sleep();
                    destoryProcess(p2);
                    sleep();
                    destoryProcess(p3);
                    count.countDown();
                }
            });

            Long p4 = select.waitForProcess();
            Long p5 = select.waitForProcess();
            initProcessIds.add(p4);
            initProcessIds.add(p5);

            sleep();
            StageMonitor monitor = ArbitrateFactory.getInstance(pipelineId, StageMonitor.class);
            List<Long> processIds = monitor.getCurrentProcessIds();
            want.collection(processIds).isEqualTo(initProcessIds);
            count.await();
            select.destory();
            ArbitrateFactory.destory(pipelineId);
        } catch (InterruptedException e) {
            want.fail();
        } finally {
            for (Long processId : initProcessIds) {
                destoryProcess(processId);
            }

        }
    }
}
