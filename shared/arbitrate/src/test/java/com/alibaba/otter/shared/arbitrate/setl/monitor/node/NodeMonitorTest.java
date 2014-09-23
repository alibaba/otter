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

package com.alibaba.otter.shared.arbitrate.setl.monitor.node;

import java.util.Arrays;
import java.util.List;

import mockit.Mock;
import mockit.Mockit;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.NodeMonitor;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * 测试下Node的监听事件
 * 
 * @author jianghang 2011-9-19 下午01:21:08
 * @version 4.0.0
 */
public class NodeMonitorTest extends BaseEventTest {

    private Long               channelId  = 100L;
    // private Long pipelineId = 100L;
    private NodeArbitrateEvent nodeEvent;
    private final Node         node1      = new Node();
    private final Node         node2      = new Node();
    private final Node         node3      = new Node();
    private final Node         node4      = new Node();
    private List<Node>         sourceList = Arrays.asList(node1, node3);
    private List<Node>         targetList = Arrays.asList(node2, node4);

    @BeforeClass
    public void init() {
        // 初始化节点
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

            @Mock
            public Pipeline getPipeline(Long pipelineId) {
                Pipeline pipeline = new Pipeline();
                pipeline.setSelectNodes(sourceList);
                pipeline.setExtractNodes(sourceList);
                pipeline.setLoadNodes(targetList);
                return pipeline;
            }

        });

        node1.setId(1L);
        node2.setId(2L);
        node3.setId(3L);
        node4.setId(4L);

        getZookeeper();
        nodeEvent = new NodeArbitrateEvent();
    }

    @BeforeMethod
    public void setUp() {
        nodeEvent.init(node1.getId());
        nodeEvent.init(node2.getId());
        nodeEvent.init(node3.getId());
        nodeEvent.init(node4.getId());
    }

    @AfterMethod
    public void tearDown() {
        nodeEvent.destory(node1.getId());
        nodeEvent.destory(node2.getId());
        nodeEvent.destory(node3.getId());
        nodeEvent.destory(node4.getId());
    }

    @Test
    public void testAliveNodes_all() {
        NodeMonitor nodeMonitor = new NodeMonitor();
        List<Long> nodes = nodeMonitor.getAliveNodes();
        want.bool(nodes.size() == 4).is(true);
        want.number(nodes.get(0)).isEqualTo(node1.getId());
        want.number(nodes.get(1)).isEqualTo(node2.getId());
        want.number(nodes.get(2)).isEqualTo(node3.getId());
        want.number(nodes.get(3)).isEqualTo(node4.getId());
    }

    @Test
    public void testAliveNodes_dead() {
        NodeMonitor nodeMonitor = new NodeMonitor();
        nodeEvent.destory(node1.getId()); // 关闭一个节点
        sleep(); // 需要间隔一定的时间，zookeeper需要推送数据到NodeMonitor，时间间隔在10ms以内
        List<Long> nodes = nodeMonitor.getAliveNodes();
        want.bool(nodes.size() == 3).is(true);
        want.number(nodes.get(0)).isEqualTo(node2.getId());
        want.number(nodes.get(1)).isEqualTo(node3.getId());
        want.number(nodes.get(2)).isEqualTo(node4.getId());

        nodeEvent.init(node1.getId()); // 开启一个节点
        nodeEvent.destory(node3.getId()); // 关闭一个节点
        sleep();
        nodes = nodeMonitor.getAliveNodes();
        want.bool(nodes.size() == 3).is(true);
        want.number(nodes.get(0)).isEqualTo(node1.getId());
        want.number(nodes.get(1)).isEqualTo(node2.getId());
        want.number(nodes.get(2)).isEqualTo(node4.getId());
    }

}
