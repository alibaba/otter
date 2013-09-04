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

package com.alibaba.otter.shared.arbitrate.demo;

import java.util.Arrays;

import mockit.Mock;
import mockit.Mockit;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.ArbitrateEventService;
import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.demo.servcie.ExtractServiceDemo;
import com.alibaba.otter.shared.arbitrate.demo.servcie.LoadServiceDemo;
import com.alibaba.otter.shared.arbitrate.demo.servcie.MainStemServiceDemo;
import com.alibaba.otter.shared.arbitrate.demo.servcie.ProcessViewDemo;
import com.alibaba.otter.shared.arbitrate.demo.servcie.SelectServiceDemo;
import com.alibaba.otter.shared.arbitrate.demo.servcie.TerminProcessDemo;
import com.alibaba.otter.shared.arbitrate.demo.servcie.TransformServiceDemo;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.communication.ArbitrateCommmunicationClient;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.PipelineArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.lb.ExtractRandomLoadBanlance;
import com.alibaba.otter.shared.arbitrate.impl.setl.lb.ExtractRoundRobinLoadBalance;
import com.alibaba.otter.shared.arbitrate.impl.setl.lb.TransformRandomLoadBanlance;
import com.alibaba.otter.shared.arbitrate.impl.setl.lb.TransformRoundRobinLoadBalance;
import com.alibaba.otter.shared.arbitrate.impl.setl.memory.MemoryStageController;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.impl.setl.rpc.RpcStageController;
import com.alibaba.otter.shared.arbitrate.impl.setl.rpc.monitor.SelectProcessListener;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.ExtractStageListener;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.LoadStageListener;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.SelectStageListener;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.TransformStageListener;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter.ArbitrateMode;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * demo的启动入口
 * 
 * @author jianghang 2011-9-22 下午03:58:53
 * @version 4.0.0
 */
public class ArbitrateAllTest extends BaseEventTest {

    private MainStemServiceDemo    mainStem;
    private SelectServiceDemo      select;
    private ExtractServiceDemo     extract;
    private TransformServiceDemo   transform;
    private LoadServiceDemo        load;
    private ProcessViewDemo        view;
    private TerminProcessDemo      termin;

    // 环境数据准备对象
    private NodeArbitrateEvent     nodeEvent;
    private ChannelArbitrateEvent  channelEvent;
    private PipelineArbitrateEvent pipelineEvent;
    private ArbitrateEventService  arbitrateEventService;
    private final Node             local         = new Node();
    private final Long             nid           = 1L;
    private Long                   channelId     = 100L;
    private Long                   pipelineId    = 100L;
    private ZkClientx              zookeeper;
    private ArbitrateMode          arbitrateMode = ArbitrateMode.RPC;

    @BeforeMethod
    public void setUp() {
        // mock 配置信息数据
        Mockit.setUpMock(ArbitrateConfigUtils.class, new Object() {

            @Mock
            public Channel getChannelByChannelId(Long channelId) {
                Channel channel = new Channel();
                channel.setId(channelId);
                Pipeline pipeline = new Pipeline();
                pipeline.setId(pipelineId);
                pipeline.setSelectNodes(Arrays.asList(local));
                pipeline.setExtractNodes(Arrays.asList(local));
                pipeline.setLoadNodes(Arrays.asList(local));
                pipeline.getParameters().setArbitrateMode(arbitrateMode);
                channel.setPipelines(Arrays.asList(pipeline));
                return channel;
            }

            @Mock
            public Pipeline getPipeline(Long pipelineId) {
                Pipeline pipeline = new Pipeline();
                pipeline.setId(pipelineId);
                pipeline.setSelectNodes(Arrays.asList(local));
                pipeline.setExtractNodes(Arrays.asList(local));
                pipeline.setLoadNodes(Arrays.asList(local));
                pipeline.getParameters().setArbitrateMode(arbitrateMode);
                return pipeline;
            }

            @Mock
            public Long getCurrentNid() {
                return nid;
            }

            @Mock
            public int getParallelism(Long pipelineId) {
                return 3;// 并行度
            }

            @Mock
            public Pipeline getOppositePipeline(Long pipelineId) {
                return null;
            }

            @Mock
            public Channel getChannel(Long pipelineId) {
                Channel channel = new Channel();
                channel.setId(channelId);
                Pipeline pipeline = new Pipeline();
                pipeline.setId(pipelineId);
                pipeline.getParameters().setArbitrateMode(arbitrateMode);
                channel.setPipelines(Arrays.asList(pipeline));
                return channel;
            }

        });

        Mockit.setUpMock(ArbitrateCommmunicationClient.class, new Object() {

            @Mock
            public Object callManager(final Event event) {
                // do nothing
                return null;
            }
        });

        zookeeper = getZookeeper();

        local.setId(nid);
        nodeEvent = new NodeArbitrateEvent();
        channelEvent = new ChannelArbitrateEvent();
        pipelineEvent = new PipelineArbitrateEvent();

        // 创建node节点
        nodeEvent.init(local.getId());

        // 创建pipeline节点
        channelEvent.init(channelId);
        pipelineEvent.init(channelId, pipelineId);

        arbitrateEventService = (ArbitrateEventService) getBeanFactory().getBean("arbitrateEventService");
        mainStem = new MainStemServiceDemo();
        autowire(mainStem);
        select = new SelectServiceDemo();
        autowire(select);
        extract = new ExtractServiceDemo();
        autowire(extract);
        transform = new TransformServiceDemo();
        autowire(transform);
        load = new LoadServiceDemo();
        autowire(load);
        view = new ProcessViewDemo();
        autowire(view);
        termin = new TerminProcessDemo();
        autowire(termin);
    }

    @AfterMethod
    public void tearDown() {
        // 删除mainStem节点
        String path = StagePathUtils.getPipeline(pipelineId) + "/" + ArbitrateConstants.NODE_MAINSTEM;
        zookeeper.delete(path);

        nodeEvent.destory(local.getId());
        // 关闭pipeline
        pipelineEvent.destory(channelId, pipelineId);
        channelEvent.destory(channelId);
    }

    @Test
    public void testDemo() {
        // 设置启动标志
        channelEvent.start(channelId);
        sleep(); // 停顿一下

        PermitMonitor permit = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);

        // 优先启动主导线程
        mainStem.submit(pipelineId);
        try {
            permit.waitForPermit();// 阻塞等待授权
        } catch (InterruptedException e1) {
            want.fail();
        }

        if (arbitrateMode.isZookeeper()) {
            ArbitrateFactory.getInstance(pipelineId, SelectStageListener.class);
            ArbitrateFactory.getInstance(pipelineId, ExtractStageListener.class);
            ArbitrateFactory.getInstance(pipelineId, TransformStageListener.class);
            ArbitrateFactory.getInstance(pipelineId, LoadStageListener.class);
        }

        if (arbitrateMode.isMemory()) {
            ArbitrateFactory.getInstance(pipelineId, MemoryStageController.class);
        }

        if (arbitrateMode.isRpc()) {
            ArbitrateFactory.getInstance(pipelineId, RpcStageController.class);
            ArbitrateFactory.getInstance(pipelineId, SelectProcessListener.class);
        }

        if (arbitrateMode.isZookeeper() || arbitrateMode.isRpc()) {
            ArbitrateFactory.getInstance(pipelineId, ExtractRandomLoadBanlance.class);
            ArbitrateFactory.getInstance(pipelineId, ExtractRoundRobinLoadBalance.class);
            ArbitrateFactory.getInstance(pipelineId, TransformRandomLoadBanlance.class);
            ArbitrateFactory.getInstance(pipelineId, TransformRoundRobinLoadBalance.class);
        }

        // 启动
        select.submit(pipelineId);
        extract.submit(pipelineId);
        transform.submit(pipelineId);
        load.submit(pipelineId);
        view.submit(pipelineId);
        this.termin.submit(pipelineId);

        try {
            Thread.sleep(20 * 1000);// 运行30s
        } catch (InterruptedException e) {
            want.fail();
        }

        // 发送结束事件
        TerminEventData termin = new TerminEventData();
        termin.setPipelineId(pipelineId);
        termin.setType(TerminType.SHUTDOWN);
        arbitrateEventService.terminEvent().single(termin);
        sleep(5 * 1000L);// 等待处理完所有的termin事件

        // 关闭
        select.destory(pipelineId);
        extract.destory(pipelineId);
        transform.destory(pipelineId);
        load.destory(pipelineId);
        view.destory(pipelineId);
        this.termin.destory(pipelineId);
        ArbitrateFactory.destory(pipelineId);
    }
}
