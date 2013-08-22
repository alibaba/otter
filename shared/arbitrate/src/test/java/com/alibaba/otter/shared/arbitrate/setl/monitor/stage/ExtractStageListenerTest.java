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

import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.ExtractStageListener;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.StageMonitor;
import com.alibaba.otter.shared.arbitrate.setl.BaseStageTest;
import com.google.common.collect.Lists;

/**
 * extract模块的测试
 * 
 * @author jianghang 2011-9-22 上午09:16:53
 * @version 4.0.0
 */
public class ExtractStageListenerTest extends BaseStageTest {

    @Test
    public void testProcess_init() {
        final List<Long> initProcessIds = new ArrayList<Long>();
        final Map<Long, List<String>> stages = new HashMap<Long, List<String>>();
        try {
            Long p1 = initProcess();
            initStage(p1, ArbitrateConstants.NODE_SELECTED);
            initStage(p1, ArbitrateConstants.NODE_EXTRACTED);

            Long p2 = initProcess();
            initStage(p2, ArbitrateConstants.NODE_SELECTED, getData(nid));

            Long p3 = initProcess();
            initStage(p3, ArbitrateConstants.NODE_SELECTED);
            initStage(p3, ArbitrateConstants.NODE_EXTRACTED);
            initStage(p3, ArbitrateConstants.NODE_TRANSFORMED);

            Long p4 = initProcess();
            initStage(p4, ArbitrateConstants.NODE_SELECTED, getData(nid + 1));

            // 准备清理数据
            initProcessIds.add(p1);
            initProcessIds.add(p2);
            initProcessIds.add(p3);
            initProcessIds.add(p4);

            List<String> p1Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED, ArbitrateConstants.NODE_EXTRACTED);
            stages.put(p1, p1Stages);

            List<String> p2Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED);
            stages.put(p2, p2Stages);

            List<String> p3Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED, ArbitrateConstants.NODE_EXTRACTED,
                                                  ArbitrateConstants.NODE_TRANSFORMED);
            stages.put(p3, p3Stages);

            List<String> p4Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED);
            stages.put(p4, p4Stages);

            // 进行验证
            ExtractStageListener extract = new ExtractStageListener(pipelineId);
            Long processId = extract.waitForProcess();
            want.number(processId).isEqualTo(p2);

            // 验证下process信息
            StageMonitor monitor = ArbitrateFactory.getInstance(pipelineId, StageMonitor.class);
            List<Long> processIds = monitor.getCurrentProcessIds();
            // 获取下stage信息
            List<String> currentP1Stages = monitor.getCurrentStages(p1);
            List<String> currentP2Stages = monitor.getCurrentStages(p2);
            List<String> currentP3Stages = monitor.getCurrentStages(p3);
            List<String> currentP4Stages = monitor.getCurrentStages(p4);

            want.collection(processIds).isEqualTo(initProcessIds);
            want.collection(currentP1Stages).isEqualTo(stages.get(p1));
            want.collection(currentP2Stages).isEqualTo(stages.get(p2));
            want.collection(currentP3Stages).isEqualTo(stages.get(p3));
            want.collection(currentP4Stages).isEqualTo(stages.get(p4));
            extract.destory();
            ArbitrateFactory.destory(pipelineId);
        } catch (InterruptedException e) {
            want.fail();
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
    public void testProcess_dynamic() {
        final List<Long> initProcessIds = new ArrayList<Long>();
        final Map<Long, List<String>> stages = new HashMap<Long, List<String>>();
        try {
            Long p1 = initProcess();
            initStage(p1, ArbitrateConstants.NODE_SELECTED);
            initStage(p1, ArbitrateConstants.NODE_EXTRACTED);
            initStage(p1, ArbitrateConstants.NODE_TRANSFORMED);

            Long p2 = initProcess();
            initStage(p2, ArbitrateConstants.NODE_SELECTED);
            initStage(p2, ArbitrateConstants.NODE_EXTRACTED);

            Long p3 = initProcess();
            initStage(p3, ArbitrateConstants.NODE_SELECTED, getData(nid + 1));

            // 初始化信息
            ExtractStageListener extract = new ExtractStageListener(pipelineId);

            // 开始变化
            destoryStage(p1, ArbitrateConstants.NODE_SELECTED);
            destoryStage(p1, ArbitrateConstants.NODE_EXTRACTED);
            destoryStage(p1, ArbitrateConstants.NODE_TRANSFORMED);
            destoryProcess(p1);
            Long p4 = initProcess();
            initStage(p4, ArbitrateConstants.NODE_SELECTED, getData(nid));

            Long p5 = initProcess();
            initStage(p5, ArbitrateConstants.NODE_SELECTED, getData(nid + 1));

            Long p6 = initProcess();

            // 准备清理数据
            initProcessIds.add(p2);
            initProcessIds.add(p3);
            initProcessIds.add(p4);
            initProcessIds.add(p5);
            initProcessIds.add(p6);

            List<String> p1Stages = Lists.newArrayList();
            stages.put(p1, p1Stages);

            List<String> p2Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED, ArbitrateConstants.NODE_EXTRACTED);
            stages.put(p2, p2Stages);

            List<String> p3Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED);
            stages.put(p3, p3Stages);

            List<String> p4Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED);
            stages.put(p4, p4Stages);

            List<String> p5Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED);
            stages.put(p5, p5Stages);

            List<String> p6Stages = Lists.newArrayList();
            stages.put(p6, p6Stages);

            sleep();// sleep一下，等待数据同步
            // 进行验证
            Long processId = extract.waitForProcess();
            want.number(processId).isEqualTo(p4);

            // 验证下process信息
            StageMonitor monitor = ArbitrateFactory.getInstance(pipelineId, StageMonitor.class);
            List<Long> processIds = monitor.getCurrentProcessIds();
            // 获取下stage信息
            List<String> currentP1Stages = monitor.getCurrentStages(p1);
            List<String> currentP2Stages = monitor.getCurrentStages(p2);
            List<String> currentP3Stages = monitor.getCurrentStages(p3);
            List<String> currentP4Stages = monitor.getCurrentStages(p4);
            List<String> currentP5Stages = monitor.getCurrentStages(p5);
            List<String> currentP6Stages = monitor.getCurrentStages(p6);

            want.collection(processIds).isEqualTo(initProcessIds);
            want.collection(currentP1Stages).isEqualTo(stages.get(p1));
            want.collection(currentP2Stages).isEqualTo(stages.get(p2));
            want.collection(currentP3Stages).isEqualTo(stages.get(p3));
            want.collection(currentP4Stages).isEqualTo(stages.get(p4));
            want.collection(currentP5Stages).isEqualTo(stages.get(p5));
            want.collection(currentP6Stages).isEqualTo(stages.get(p6));
            extract.destory();
            ArbitrateFactory.destory(pipelineId);
        } catch (InterruptedException e) {
            want.fail();
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
