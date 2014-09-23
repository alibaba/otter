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

package com.alibaba.otter.shared.arbitrate.setl.event.zookeeper;

import java.util.ArrayList;
import java.util.List;

import mockit.Mock;
import mockit.Mockit;

import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.ExtractZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.SelectZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.TransformZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.setl.event.BaseArbitrateEventTest;

/**
 * @author jianghang 2011-9-23 下午02:58:13
 * @version 4.0.0
 */
public class TransformArbitrateEventTest extends BaseArbitrateEventTest {

    private SelectZooKeeperArbitrateEvent    selectEvent;
    private ExtractZooKeeperArbitrateEvent   extractEvent;
    private TransformZooKeeperArbitrateEvent transformEvent;

    @Test
    public void test_transform() {
        Mockit.setUpMock(ArbitrateConfigUtils.class, new Object() {

            @Mock
            public int getParallelism(Long pipelineId) {
                return 2;// 并行度
            }

        });

        selectEvent = new SelectZooKeeperArbitrateEvent();
        extractEvent = new ExtractZooKeeperArbitrateEvent();
        transformEvent = new TransformZooKeeperArbitrateEvent();

        final List<Long> initProcessIds = new ArrayList<Long>();
        try {
            // 获取数据

            // select stage
            EtlEventData sdata1 = selectEvent.await(pipelineId);
            EtlEventData sdata2 = selectEvent.await(pipelineId);

            initProcessIds.add(sdata1.getProcessId());
            initProcessIds.add(sdata2.getProcessId());

            selectEvent.single(sdata1);
            selectEvent.single(sdata2);
            // extract stage
            EtlEventData edata1 = extractEvent.await(pipelineId);
            EtlEventData edata2 = extractEvent.await(pipelineId);

            extractEvent.single(edata1);
            extractEvent.single(edata2);

            // transform stage
            EtlEventData tdata1 = transformEvent.await(pipelineId);
            EtlEventData tdata2 = transformEvent.await(pipelineId);

            transformEvent.single(tdata1);
            transformEvent.single(tdata2);
            ArbitrateFactory.destory(pipelineId);
        } catch (InterruptedException e) {
            want.fail();
        } finally {
            for (Long processId : initProcessIds) {
                destoryStage(processId, ArbitrateConstants.NODE_SELECTED);
                destoryStage(processId, ArbitrateConstants.NODE_EXTRACTED);
                destoryStage(processId, ArbitrateConstants.NODE_TRANSFORMED);
                destoryProcess(processId);
            }
        }
    }
}
