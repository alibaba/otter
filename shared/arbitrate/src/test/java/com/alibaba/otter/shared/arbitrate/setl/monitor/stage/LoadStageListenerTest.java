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
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.LoadStageListener;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.StageMonitor;
import com.alibaba.otter.shared.arbitrate.setl.BaseStageTest;
import com.google.common.collect.Lists;

/**
 * load模块的测试
 * 
 * @author jianghang 2011-9-22 上午10:35:18
 * @version 4.0.0
 */
public class LoadStageListenerTest extends BaseStageTest {

    @Test
    public void testProcess_init() {
        final List<Long> initProcessIds = new ArrayList<Long>();
        final Map<Long, List<String>> stages = new HashMap<Long, List<String>>();
        try {
            // 准备数据
            Long p1 = initProcess();
            initStage(p1, ArbitrateConstants.NODE_SELECTED);
            initStage(p1, ArbitrateConstants.NODE_EXTRACTED);
            initStage(p1, ArbitrateConstants.NODE_TRANSFORMED, getData(nid));

            Long p2 = initProcess();
            initStage(p2, ArbitrateConstants.NODE_SELECTED);
            initStage(p2, ArbitrateConstants.NODE_EXTRACTED);
            initStage(p2, ArbitrateConstants.NODE_TRANSFORMED, getData(nid + 1));

            // 准备清理数据
            initProcessIds.add(p1);
            initProcessIds.add(p2);

            List<String> p1Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED, ArbitrateConstants.NODE_EXTRACTED,
                                                  ArbitrateConstants.NODE_TRANSFORMED);
            stages.put(p1, p1Stages);

            List<String> p2Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED, ArbitrateConstants.NODE_EXTRACTED,
                                                  ArbitrateConstants.NODE_TRANSFORMED);
            stages.put(p2, p2Stages);

            // 进行验证
            LoadStageListener load = new LoadStageListener(pipelineId);
            Long processId = load.waitForProcess();
            want.number(processId).isEqualTo(p1);

            // 验证下process信息
            StageMonitor monitor = ArbitrateFactory.getInstance(pipelineId, StageMonitor.class);
            List<Long> processIds = monitor.getCurrentProcessIds();
            // 获取下stage信息
            List<String> currentP1Stages = monitor.getCurrentStages(p1);
            List<String> currentP2Stages = monitor.getCurrentStages(p2);

            want.collection(processIds).isEqualTo(initProcessIds);
            want.collection(currentP1Stages).isEqualTo(stages.get(p1));
            want.collection(currentP2Stages).isEqualTo(stages.get(p2));
            load.destory();
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
    public void testProcess_dymanic() {
        final List<Long> initProcessIds = new ArrayList<Long>();
        final Map<Long, List<String>> stages = new HashMap<Long, List<String>>();
        try {
            // 准备数据
            Long p1 = initProcess();
            initStage(p1, ArbitrateConstants.NODE_SELECTED);
            initStage(p1, ArbitrateConstants.NODE_EXTRACTED);
            initStage(p1, ArbitrateConstants.NODE_TRANSFORMED, getData(nid + 1));

            Long p2 = initProcess();
            initStage(p2, ArbitrateConstants.NODE_SELECTED);
            initStage(p2, ArbitrateConstants.NODE_EXTRACTED);
            initStage(p2, ArbitrateConstants.NODE_TRANSFORMED, getData(nid));

            Long p3 = initProcess();
            initStage(p3, ArbitrateConstants.NODE_SELECTED);
            initStage(p3, ArbitrateConstants.NODE_EXTRACTED);
            initStage(p3, ArbitrateConstants.NODE_TRANSFORMED, getData(nid));

            // 初始化
            LoadStageListener load = new LoadStageListener(pipelineId);

            // 开始变化
            // initStage(p1, ArbitrateConstants.NODE_LOADED);
            destoryStage(p1, ArbitrateConstants.NODE_SELECTED);
            destoryStage(p1, ArbitrateConstants.NODE_EXTRACTED);
            destoryStage(p1, ArbitrateConstants.NODE_TRANSFORMED);
            // destoryStage(p1, ArbitrateConstants.NODE_LOADED);
            destoryProcess(p1);

            // 准备清理数据
            initProcessIds.add(p2);
            initProcessIds.add(p3);

            List<String> p1Stages = Lists.newArrayList();
            stages.put(p1, p1Stages);

            List<String> p2Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED, ArbitrateConstants.NODE_EXTRACTED,
                                                  ArbitrateConstants.NODE_TRANSFORMED);
            stages.put(p2, p2Stages);

            List<String> p3Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED, ArbitrateConstants.NODE_EXTRACTED,
                                                  ArbitrateConstants.NODE_TRANSFORMED);
            stages.put(p3, p3Stages);

            sleep();
            // 进行验证
            Long processId = load.waitForProcess();
            want.number(processId).isEqualTo(p2);

            // 验证下process信息
            StageMonitor monitor = ArbitrateFactory.getInstance(pipelineId, StageMonitor.class);
            List<Long> processIds = monitor.getCurrentProcessIds();
            // 获取下stage信息
            List<String> currentP1Stages = monitor.getCurrentStages(p1);
            List<String> currentP2Stages = monitor.getCurrentStages(p2);
            List<String> currentP3Stages = monitor.getCurrentStages(p3);

            want.collection(processIds).isEqualTo(initProcessIds);
            want.collection(currentP1Stages).isEqualTo(stages.get(p1));
            want.collection(currentP2Stages).isEqualTo(stages.get(p2));
            want.collection(currentP3Stages).isEqualTo(stages.get(p3));

            // 继续变化
            // initStage(p2, ArbitrateConstants.NODE_LOADED);
            destoryStage(p2, ArbitrateConstants.NODE_SELECTED);
            destoryStage(p2, ArbitrateConstants.NODE_EXTRACTED);
            destoryStage(p2, ArbitrateConstants.NODE_TRANSFORMED);
            // destoryStage(p2, ArbitrateConstants.NODE_LOADED);
            destoryProcess(p2);

            // 准备清理数据
            initProcessIds.remove(p2);

            p2Stages = Lists.newArrayList();
            stages.put(p2, p2Stages);

            sleep();
            // 进行验证
            processId = load.waitForProcess();
            want.number(processId).isEqualTo(p3);

            // 验证下process信息
            processIds = monitor.getCurrentProcessIds();
            // 获取下stage信息
            currentP1Stages = monitor.getCurrentStages(p1);
            currentP2Stages = monitor.getCurrentStages(p2);
            currentP3Stages = monitor.getCurrentStages(p3);

            want.collection(processIds).isEqualTo(initProcessIds);
            want.collection(currentP1Stages).isEqualTo(stages.get(p1));
            want.collection(currentP2Stages).isEqualTo(stages.get(p2));
            want.collection(currentP3Stages).isEqualTo(stages.get(p3));
            load.destory();
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
