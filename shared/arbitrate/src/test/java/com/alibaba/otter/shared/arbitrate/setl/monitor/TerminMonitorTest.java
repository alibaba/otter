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

package com.alibaba.otter.shared.arbitrate.setl.monitor;

import mockit.Mock;
import mockit.Mockit;

import org.apache.zookeeper.CreateMode;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.PipelineArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.MonitorScheduler;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.TerminMonitor;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * @author jianghang 2011-9-27 上午11:15:21
 * @version 4.0.0
 */
public class TerminMonitorTest extends BaseEventTest {

    private ZkClientx              zookeeper  = null;
    private ChannelArbitrateEvent  channelEvent;
    private PipelineArbitrateEvent pipelineEvent;

    private Long                   channelId  = 100L;
    private Long                   pipelineId = 100L;

    private TerminMonitor          terminMonitor;

    @BeforeClass
    public void init() {
        // 初始化节点
        // mock 配置信息数据
        Mockit.setUpMock(ArbitrateConfigUtils.class, new Object() {

            @Mock
            public Channel getChannel(Long pipelineId) {
                Channel channel = new Channel();
                channel.setId(channelId);
                return channel;
            }

            @Mock
            public Pipeline getOppositePipeline(Long pipelineId) {
                Pipeline pipeline = new Pipeline();
                pipeline.setId(pipelineId);
                return pipeline;
            }

        });

        zookeeper = getZookeeper();
        channelEvent = new ChannelArbitrateEvent();
        pipelineEvent = new PipelineArbitrateEvent();
    }

    @BeforeMethod
    public void setUp() {
        channelEvent.init(channelId);
        // 初始化两个pipeline
        pipelineEvent.init(channelId, pipelineId);
    }

    @AfterMethod
    public void tearDown() {
        MonitorScheduler.unRegister(terminMonitor);
        // 初始化两个pipeline
        pipelineEvent.destory(channelId, pipelineId);
        channelEvent.destory(channelId);
    }

    @Test
    public void test_init() {
        initTermin(1L);
        initTermin(2L);
        try {
            terminMonitor = new TerminMonitor(pipelineId);
            Long p1 = terminMonitor.waitForProcess();
            terminMonitor.ack(p1);
            Long p2 = terminMonitor.waitForProcess();
            terminMonitor.ack(p2);

            want.bool(p1.equals(1L)).is(true);
            want.bool(p2.equals(2L)).is(true);
            terminMonitor.destory();
        } catch (InterruptedException e) {
            want.fail();
        } finally {
            destoryTermin(1L);
            destoryTermin(2L);
        }
    }

    @Test
    public void test_dynamic() {
        initTermin(1L);
        initTermin(2L);
        try {
            terminMonitor = new TerminMonitor(pipelineId);
            // 开始变化
            destoryTermin(1L);
            initTermin(3L);
            initTermin(1L);
            sleep();
            Long p1 = terminMonitor.waitForProcess();
            terminMonitor.ack(p1);
            Long p2 = terminMonitor.waitForProcess();
            terminMonitor.ack(p2);
            Long p3 = terminMonitor.waitForProcess();
            terminMonitor.ack(p3);
            // 一定是按顺序输出
            want.bool(p1.equals(1L)).is(true);
            want.bool(p2.equals(2L)).is(true);
            want.bool(p3.equals(3L)).is(true);
            terminMonitor.destory();
        } catch (InterruptedException e) {
            want.fail();
        } finally {
            destoryTermin(1L);
            destoryTermin(2L);
            destoryTermin(3L);
        }
    }

    private void initTermin(Long processId) {
        TerminEventData data = new TerminEventData();
        data.setPipelineId(pipelineId);
        data.setProcessId(processId);
        data.setType(TerminType.NORMAL);

        byte[] bytes = JsonUtils.marshalToByte(data);
        zookeeper.create(StagePathUtils.getTermin(pipelineId, processId), bytes, CreateMode.PERSISTENT);

    }

    private void destoryTermin(Long processId) {
        zookeeper.delete(StagePathUtils.getTermin(pipelineId, processId));
    }
}
