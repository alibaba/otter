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

package com.alibaba.otter.shared.arbitrate.setl.event.memory;

import java.util.ArrayList;
import java.util.List;

import mockit.Mock;
import mockit.Mockit;

import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.memory.ExtractMemoryArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.memory.LoadMemoryArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.memory.SelectMemoryArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.memory.TerminMemoryArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.memory.TransformMemoryArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.arbitrate.setl.event.BaseArbitrateEventTest;

/**
 * @author jianghang 2011-9-27 下午09:45:23
 * @version 4.0.0
 */
public class TerminMemoryArbitrateEventTest extends BaseArbitrateEventTest {

    private SelectMemoryArbitrateEvent    selectEvent;
    private ExtractMemoryArbitrateEvent   extractEvent;
    private TransformMemoryArbitrateEvent transformEvent;
    private LoadMemoryArbitrateEvent      loadEvent;
    private TerminMemoryArbitrateEvent    terminEvent;

    @Test
    public void test_Rollback() {
        normalProcess();
        // 发送rollback信号
        TerminEventData rollback = new TerminEventData();
        rollback.setPipelineId(pipelineId);
        rollback.setType(TerminType.ROLLBACK);
        terminEvent.single(rollback);

        PermitMonitor monitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        want.bool(monitor.getChannelPermit(true).isPause()).is(true);

        destoryTermin();
        ArbitrateFactory.destory(pipelineId);
    }

    @Test
    public void test_Shutdown() {
        normalProcess();
        // 发送shutdown信号
        TerminEventData shutdown = new TerminEventData();
        shutdown.setPipelineId(pipelineId);
        shutdown.setType(TerminType.SHUTDOWN);
        terminEvent.single(shutdown);

        PermitMonitor monitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        want.bool(monitor.getChannelPermit(true).isStop()).is(true);

        destoryTermin();
        ArbitrateFactory.destory(pipelineId);
    }

    @Test
    public void test_Restart() {
        normalProcess();
        // 发送restart信号
        TerminEventData rollback = new TerminEventData();
        rollback.setPipelineId(pipelineId);
        rollback.setType(TerminType.RESTART);
        terminEvent.single(rollback);

        sleep(4000L);
        PermitMonitor monitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        want.bool(monitor.getChannelPermit(true).isStart()).is(true);
        sleep(4000L);

        // 发送shutdown信号
        TerminEventData shutdown = new TerminEventData();
        shutdown.setPipelineId(pipelineId);
        shutdown.setType(TerminType.SHUTDOWN);
        terminEvent.single(shutdown);

        want.bool(monitor.getChannelPermit(true).isStop()).is(true);

        // 删除对应的错误节点
        destoryTermin();
        ArbitrateFactory.destory(pipelineId);
    }

    private void destoryTermin() {
        String path = StagePathUtils.getTerminRoot(pipelineId);
        List<String> terminNodes = zookeeper.getChildren(path);
        for (String node : terminNodes) {
            // 删除对应的错误节点
            TerminEventData termin = new TerminEventData();
            termin.setPipelineId(pipelineId);
            termin.setProcessId(StagePathUtils.getProcessId(node));
            System.out.println("remove termin node: " + path + "/" + node);
            terminEvent.ack(termin);// 发送ack信号，删除termin节点
        }
    }

    private void normalProcess() {
        Mockit.setUpMock(ArbitrateConfigUtils.class, new Object() {

            @Mock
            public int getParallelism(Long pipelineId) {
                return 2;// 并行度
            }

            @Mock
            public Long getCurrentNid() {
                return nid;
            }

        });

        selectEvent = new SelectMemoryArbitrateEvent();
        extractEvent = new ExtractMemoryArbitrateEvent();
        transformEvent = new TransformMemoryArbitrateEvent();
        loadEvent = new LoadMemoryArbitrateEvent();
        terminEvent = (TerminMemoryArbitrateEvent) this.getBeanFactory().getBean("terminMemoryEvent");
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

            // SelectStageListener selectStageListener =
            // ArbitrateFactory.getInstance(pipelineId,
            // SelectStageListener.class);
            // selectStageListener.destory();
            // load stage
            EtlEventData ldata1 = loadEvent.await(pipelineId);
            loadEvent.single(ldata1);
            Long p1 = ldata1.getProcessId();

            TerminEventData terminData1 = new TerminEventData();
            terminData1.setPipelineId(pipelineId);
            terminData1.setProcessId(p1);
            terminEvent.ack(terminData1);// 发送ack信号，删除termin节点

        } catch (InterruptedException e) {
            want.fail();
        }

    }
}
