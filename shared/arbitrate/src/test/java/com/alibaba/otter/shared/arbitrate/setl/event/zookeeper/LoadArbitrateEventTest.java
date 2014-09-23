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

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.ExtractZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.LoadZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.SelectZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.TerminZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.TransformZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.SelectStageListener;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.setl.event.BaseArbitrateEventTest;

/**
 * @author jianghang 2011-9-23 下午02:58:13
 * @version 4.0.0
 */
public class LoadArbitrateEventTest extends BaseArbitrateEventTest {

    private SelectZooKeeperArbitrateEvent    selectEvent;
    private ExtractZooKeeperArbitrateEvent   extractEvent;
    private TransformZooKeeperArbitrateEvent transformEvent;
    private LoadZooKeeperArbitrateEvent      loadEvent;
    private TerminZooKeeperArbitrateEvent    terminEvent;

    @Test
    public void test_load() {
        Mockit.setUpMock(ArbitrateConfigUtils.class, new Object() {

            @Mock
            public int getParallelism(Long pipelineId) {
                return 2;// 并行度
            }

        });

        selectEvent = new SelectZooKeeperArbitrateEvent();
        extractEvent = new ExtractZooKeeperArbitrateEvent();
        transformEvent = new TransformZooKeeperArbitrateEvent();
        loadEvent = new LoadZooKeeperArbitrateEvent();
        terminEvent = (TerminZooKeeperArbitrateEvent) this.getBeanFactory().getBean("terminZooKeeperEvent");
        loadEvent.setTerminEvent(terminEvent);

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

            SelectStageListener selectStageListener = ArbitrateFactory.getInstance(pipelineId,
                                                                                   SelectStageListener.class);
            selectStageListener.destory();
            // load stage
            EtlEventData ldata1 = loadEvent.await(pipelineId);
            loadEvent.single(ldata1);
            Long p1 = ldata1.getProcessId();

            TerminEventData terminData1 = new TerminEventData();
            terminData1.setPipelineId(pipelineId);
            terminData1.setProcessId(p1);
            terminEvent.ack(terminData1);// 发送ack信号，删除termin节点

            EtlEventData ldata2 = loadEvent.await(pipelineId);
            want.bool(ldata1.getProcessId() < ldata2.getProcessId()).is(true);
            loadEvent.single(ldata2);

            Long p2 = ldata2.getProcessId();
            TerminEventData terminData2 = new TerminEventData();
            terminData2.setPipelineId(pipelineId);
            terminData2.setProcessId(p2);
            terminEvent.ack(terminData2);// 发送ack信号，删除termin节点

            sleep(2000L);
            ArbitrateFactory.destory(pipelineId);
        } catch (InterruptedException e) {
            want.fail();
        } finally {
        }
    }
}
