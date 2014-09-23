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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.ArbitrateViewService;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.setl.BaseStageTest;
import com.alibaba.otter.shared.common.model.statistics.stage.ProcessStat;

/**
 * @author jianghang 2011-9-28 下午04:43:02
 * @version 4.0.0
 */
public class ArbitrateViewServiceTest extends BaseStageTest {

    @SpringBeanByName
    private ArbitrateViewService arbitrateViewService;

    @Test
    public void test_listProcesses() {
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
            initStage(p3, ArbitrateConstants.NODE_SELECTED);

            Long p4 = initProcess();

            initProcessIds.add(p1);
            initProcessIds.add(p2);
            initProcessIds.add(p3);
            initProcessIds.add(p4);
            List<String> p1Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED, ArbitrateConstants.NODE_EXTRACTED,
                                                  ArbitrateConstants.NODE_TRANSFORMED);
            stages.put(p1, p1Stages);

            List<String> p2Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED, ArbitrateConstants.NODE_EXTRACTED);
            stages.put(p2, p2Stages);

            List<String> p3Stages = Arrays.asList(ArbitrateConstants.NODE_SELECTED);
            stages.put(p3, p3Stages);

            List<String> p4Stages = new ArrayList<String>();
            stages.put(p4, p4Stages);

            List<ProcessStat> processStat = arbitrateViewService.listProcesses(channelId, pipelineId);
            System.out.println(processStat);
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
