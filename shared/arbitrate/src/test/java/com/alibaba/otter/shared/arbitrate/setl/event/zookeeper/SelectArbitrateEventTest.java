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
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.SelectZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.setl.event.BaseArbitrateEventTest;

/**
 * @author jianghang 2011-9-22 下午05:24:26
 * @version 4.0.0
 */
public class SelectArbitrateEventTest extends BaseArbitrateEventTest {

    private SelectZooKeeperArbitrateEvent selectEvent;

    @Test
    public void test_select() {
        Mockit.setUpMock(ArbitrateConfigUtils.class, new Object() {

            @Mock
            public int getParallelism(Long pipelineId) {
                return 2;//并行度
            }

        });

        selectEvent = new SelectZooKeeperArbitrateEvent();
        final List<Long> initProcessIds = new ArrayList<Long>();
        try {
            //获取数据
            EtlEventData data1 = selectEvent.await(pipelineId);
            EtlEventData data2 = selectEvent.await(pipelineId);

            want.bool(data1 != null).is(true);
            want.bool(data2 != null).is(true);
            initProcessIds.add(data1.getProcessId());
            initProcessIds.add(data2.getProcessId());

            selectEvent.single(data1);
            selectEvent.single(data2);

            ArbitrateFactory.destory(pipelineId);
        } catch (InterruptedException e) {
            want.fail();
        } finally {
            for (Long processId : initProcessIds) {
                destoryStage(processId, ArbitrateConstants.NODE_SELECTED);
                destoryProcess(processId);
            }
        }
    }
}
