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

package com.alibaba.otter.shared.arbitrate.setl.lb;

import java.util.Arrays;
import java.util.List;

import mockit.Mock;
import mockit.Mockit;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.NodeMonitor;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * lb 测试基础
 * 
 * @author jianghang 2011-9-22 上午11:38:21
 * @version 4.0.0
 */
public class BaseLoadBalanceTest extends BaseEventTest {

    protected ZkClientx          zookeeper  = null;
    protected Long               channelId  = 100L;
    protected Long               pipelineId = 100L;
    protected NodeArbitrateEvent nodeEvent;
    protected NodeMonitor        nodeMonitor;
    protected final Node         node1      = new Node();
    protected final Node         node2      = new Node();
    protected final Node         node3      = new Node();
    protected final Node         node4      = new Node();
    protected List<Node>         sourceList = Arrays.asList(node1, node3);
    protected List<Node>         targetList = Arrays.asList(node2, node4);

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
            public Long getCurrentNid() {
                return 1L;
            }

        });

        node1.setId(1L);
        node2.setId(2L);
        node3.setId(3L);
        node4.setId(4L);
        zookeeper = getZookeeper();
        nodeEvent = new NodeArbitrateEvent();
        nodeMonitor = new NodeMonitor();
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
}
