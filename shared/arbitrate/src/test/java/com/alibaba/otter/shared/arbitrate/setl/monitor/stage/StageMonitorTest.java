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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.MonitorScheduler;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.StageListener;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.StageMonitor;
import com.alibaba.otter.shared.arbitrate.setl.BaseStageTest;

/**
 * stage监控测试
 * 
 * @author jianghang 2011-9-21 下午02:37:23
 * @version 4.0.0
 */
public class StageMonitorTest extends BaseStageTest {

    private StageMonitor monitor;

    @AfterMethod
    public void dispose() {
        MonitorScheduler.unRegister(monitor);
    }

    @Test
    public void testProcess_static() {// 测试静态的process获取
        final List<Long> initProcessIds = new ArrayList<Long>();
        try {
            Long p1 = initProcess();
            Long p2 = initProcess();
            initProcessIds.add(p1);
            initProcessIds.add(p2);
            monitor = ArbitrateFactory.getInstance(pipelineId, StageMonitor.class);
            List<Long> processIds = monitor.getCurrentProcessIds();
            want.collection(processIds).isEqualTo(initProcessIds);

            MonitorScheduler.unRegister(monitor);
        } finally {
            for (Long processId : initProcessIds) {
                destoryProcess(processId);
            }

        }
    }

    @Test
    public void testProcess_dynamic() {// 测试动态的process获取
        final List<Long> initProcessIds = new ArrayList<Long>();
        try {
            Long p1 = initProcess();
            Long p2 = initProcess();
            initProcessIds.add(p1);
            initProcessIds.add(p2);

            monitor = ArbitrateFactory.getInstance(pipelineId, StageMonitor.class);
            final CountDownLatch count = new CountDownLatch(1);
            monitor.addListener(new StageListener() {

                public void stageChannged(Long processId, List<String> stageNodes) {

                }

                public void processChanged(List<Long> processIds) {
                    count.countDown();
                    want.collection(processIds).isEqualTo(initProcessIds);
                }

                @Override
                public void processTermined(Long processId) {

                }
            });
            // 产生一些变化
            Long p3 = initProcess();
            initProcessIds.add(p3);
            destoryProcess(p1);
            initProcessIds.remove(p1);

            sleep();
            List<Long> processIds = monitor.getCurrentProcessIds();
            want.collection(processIds).isEqualTo(initProcessIds);

            try {
                count.await();// 等待listener响应
            } catch (InterruptedException e) {
                want.fail();
            }
        } finally {
            for (Long processId : initProcessIds) {
                destoryProcess(processId);
            }
        }
    }

    @Test
    public void testStage_static() {// 测试静态的stage获取
        final List<Long> initProcessIds = new ArrayList<Long>();
        final Map<Long, List<String>> stages = new HashMap<Long, List<String>>();
        try {
            Long p1 = initProcess();
            Long p2 = initProcess();
            initProcessIds.add(p1);
            initProcessIds.add(p2);

            initStage(p1, ArbitrateConstants.NODE_SELECTED);
            initStage(p1, ArbitrateConstants.NODE_EXTRACTED);
            List<String> p1Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED, ArbitrateConstants.NODE_EXTRACTED);
            stages.put(p1, p1Stages);
            initStage(p2, ArbitrateConstants.NODE_SELECTED);
            List<String> p2Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED);
            stages.put(p2, p2Stages);

            StageMonitor monitor = new StageMonitor(pipelineId);
            List<Long> processIds = monitor.getCurrentProcessIds();
            // 获取下stage信息
            List<String> currentP1Stages = monitor.getCurrentStages(p1);
            List<String> currentP2Stages = monitor.getCurrentStages(p2);

            want.collection(processIds).isEqualTo(initProcessIds);
            want.collection(currentP1Stages).isEqualTo(stages.get(p1));
            want.collection(currentP2Stages).isEqualTo(stages.get(p2));
        } finally {
            for (Long processId : initProcessIds) {
                List<String> ss = stages.get(processId);
                for (String stage : ss) {
                    destoryStage(processId, stage);
                }
                destoryProcess(processId);
            }
        }
    }

    @Test
    public void testStage_dynamic() {// 测试动态的stage获取
        final List<Long> initProcessIds = new ArrayList<Long>();
        final Map<Long, List<String>> stages = new HashMap<Long, List<String>>();
        try {
            Long p1 = initProcess();
            Long p2 = initProcess();
            initProcessIds.add(p1);
            initProcessIds.add(p2);

            initStage(p1, ArbitrateConstants.NODE_SELECTED);
            initStage(p1, ArbitrateConstants.NODE_EXTRACTED);
            initStage(p2, ArbitrateConstants.NODE_SELECTED);

            monitor = ArbitrateFactory.getInstance(pipelineId, StageMonitor.class);
            // 产生一些变化
            Long p3 = initProcess();
            initProcessIds.add(p3);
            // p1走完所有的stage后被销毁
            initStage(p1, ArbitrateConstants.NODE_TRANSFORMED);
            // initStage(p1, ArbitrateConstants.NODE_LOADED);
            destoryStage(p1, ArbitrateConstants.NODE_SELECTED);
            destoryStage(p1, ArbitrateConstants.NODE_EXTRACTED);
            destoryStage(p1, ArbitrateConstants.NODE_TRANSFORMED);
            // destoryStage(p1, ArbitrateConstants.NODE_LOADED);
            destoryProcess(p1);
            initProcessIds.remove(p1);
            // p2走了一个stage
            initStage(p2, ArbitrateConstants.NODE_EXTRACTED);

            List<String> p2Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED, ArbitrateConstants.NODE_EXTRACTED);
            stages.put(p1, new ArrayList<String>());
            stages.put(p2, p2Stages);
            stages.put(p3, new ArrayList<String>());

            sleep();
            List<Long> processIds = monitor.getCurrentProcessIds();
            // 获取下stage信息
            List<String> currentP1Stages = monitor.getCurrentStages(p1);
            List<String> currentP2Stages = monitor.getCurrentStages(p2);
            List<String> currentP3Stages = monitor.getCurrentStages(p3);

            want.collection(processIds).isEqualTo(initProcessIds);
            want.collection(currentP1Stages).isEqualTo(stages.get(p1));
            want.collection(currentP2Stages).isEqualTo(stages.get(p2));
            want.collection(currentP3Stages).isEqualTo(stages.get(p3));

        } finally {
            for (Long processId : initProcessIds) {
                List<String> ss = stages.get(processId);
                for (String stage : ss) {
                    destoryStage(processId, stage);
                }

                destoryProcess(processId);
            }
        }
    }

}
