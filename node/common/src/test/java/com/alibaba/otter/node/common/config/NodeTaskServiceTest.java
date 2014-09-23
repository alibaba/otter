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

package com.alibaba.otter.node.common.config;

import java.util.Arrays;
import java.util.List;

import mockit.Mock;
import mockit.Mocked;
import mockit.Mockit;

import org.jtester.annotations.SpringBeanFrom;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.BaseOtterTest;
import com.alibaba.otter.node.common.communication.NodeCommmunicationClient;
import com.alibaba.otter.node.common.communication.NodeCommunicationEndpoint;
import com.alibaba.otter.node.common.config.impl.NodeTaskServiceImpl;
import com.alibaba.otter.node.common.config.model.NodeTask;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.TestUtils;
import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.communication.model.config.FindNodeEvent;
import com.alibaba.otter.shared.communication.model.config.NotifyChannelEvent;
import com.google.common.collect.Lists;

public class NodeTaskServiceTest extends BaseOtterTest {

    @Mocked
    @SpringBeanFrom
    private NodeCommunicationEndpoint nodeCommunicationEndpoint;

    @BeforeClass
    public void initial() {
        System.setProperty("nid", "1");
        try {
            new NonStrictExpectations() {

                {
                    nodeCommunicationEndpoint.afterPropertiesSet();
                }
            };
        } catch (Exception e) {
            want.fail();
        }

    }

    @Test
    public void test_b_channel() {
        Long channelId = 100L;
        Long pipelineId = 101L;
        Long oppositePipelineId = 102L; // 先加一个pipeline的同步任务
        final Channel channel = new Channel();
        channel.setId(channelId);
        channel.setStatus(ChannelStatus.START);
        Pipeline pipeline1 = new Pipeline();
        pipeline1.setChannelId(channelId);
        pipeline1.setId(pipelineId);

        Pipeline pipeline2 = new Pipeline();
        pipeline2.setChannelId(channelId);
        pipeline2.setId(oppositePipelineId);
        channel.setPipelines(Arrays.asList(pipeline1, pipeline2));

        Node node1 = new Node();
        node1.setId(1L);

        Node node2 = new Node();
        node2.setId(2L);

        pipeline1.setSelectNodes(Arrays.asList(node1, node2));
        pipeline1.setExtractNodes(Arrays.asList(node1, node2));
        pipeline1.setLoadNodes(Arrays.asList(node2));

        pipeline2.setSelectNodes(Arrays.asList(node1));
        pipeline2.setExtractNodes(Arrays.asList(node1));
        pipeline2.setLoadNodes(Arrays.asList(node1, node2));
        Mockit.setUpMock(NodeCommmunicationClient.class, new Object() {

            @Mock
            public Object callManager(final Event event) {
                if (event instanceof FindNodeEvent) {
                    Node node = new Node();
                    Long nid = ((FindNodeEvent) event).getNid();
                    node.setId(nid);
                    return node;
                } else {
                    return Arrays.asList(channel);
                }
            }
        });
        // 初始化一下数据
        NodeTaskServiceImpl nodeTaskSerivce = (NodeTaskServiceImpl) spring.getBean("nodeTaskService");
        List<NodeTask> tasks = null;

        NotifyChannelEvent event = new NotifyChannelEvent();
        event.setChannel(channel);
        reflector.invoke(nodeTaskSerivce, "onNotifyChannel", event);
        tasks = reflector.invoke(nodeTaskSerivce, "mergeIncNodeTasks");
        want.bool(tasks.size() == 2).is(true);

        tasks = nodeTaskSerivce.listAllNodeTasks();
        want.bool(tasks.size() == 2).is(true);
    }

    @Test
    public void test_a_reload() throws Exception {
        Long channelId = 100L;
        Long pipelineId = 100L;
        Long oppositePipelineId = 101L;
        final Channel channel = new Channel();
        channel.setId(channelId);
        channel.setStatus(ChannelStatus.START);
        Pipeline pipeline1 = new Pipeline();
        pipeline1.setChannelId(channelId);
        pipeline1.setId(pipelineId);

        Pipeline pipeline2 = new Pipeline();
        pipeline2.setChannelId(channelId);
        pipeline2.setId(oppositePipelineId);
        channel.setPipelines(Arrays.asList(pipeline1, pipeline2));

        Node node1 = new Node();
        node1.setId(1L);

        Node node2 = new Node();
        node2.setId(2L);

        pipeline1.setSelectNodes(Arrays.asList(node1, node2));
        pipeline1.setExtractNodes(Arrays.asList(node1, node2));
        pipeline1.setLoadNodes(Arrays.asList(node1));

        pipeline2.setSelectNodes(Arrays.asList(node2));
        pipeline2.setExtractNodes(Arrays.asList(node2));
        pipeline2.setLoadNodes(Arrays.asList(node1, node2));

        Mockit.setUpMock(NodeCommmunicationClient.class, new Object() {

            @Mock
            public Object callManager(final Event event) {
                if (event instanceof FindNodeEvent) {
                    Node node = new Node();
                    Long nid = ((FindNodeEvent) event).getNid();
                    node.setId(nid);
                    return node;
                } else {
                    return Arrays.asList(channel);
                }
            }
        });
        List<NodeTask> tasks = null;
        NodeTaskServiceImpl nodeTaskSerivce = (NodeTaskServiceImpl) spring.getBean("nodeTaskService");
        reflector.invoke(nodeTaskSerivce, "initNodeTask");
        tasks = reflector.invoke(nodeTaskSerivce, "mergeIncNodeTasks");
        want.number(tasks.size()).isEqualTo(2);

        reflector.invoke(nodeTaskSerivce, "initNodeTask");
        tasks = reflector.invoke(nodeTaskSerivce, "mergeIncNodeTasks");
        want.bool(tasks.size() == 0).is(true);

        channel.setStatus(ChannelStatus.STOP);
        reflector.invoke(nodeTaskSerivce, "initNodeTask");
        tasks = reflector.invoke(nodeTaskSerivce, "mergeIncNodeTasks");
        want.bool(tasks.size() == 2).is(true);
        tasks = nodeTaskSerivce.listAllNodeTasks();
        want.bool(tasks.size() == 2).is(true);

        // 清理内存
        TestUtils.setField(nodeTaskSerivce, "allTasks", Lists.newArrayList());
        TestUtils.setField(nodeTaskSerivce, "incTasks", Lists.newArrayList());
        // 删除某个pipeline的node
        channel.setStatus(ChannelStatus.START);
        reflector.invoke(nodeTaskSerivce, "initNodeTask");
        tasks = reflector.invoke(nodeTaskSerivce, "mergeIncNodeTasks");
        want.number(tasks.size()).isEqualTo(2);

        pipeline1.setSelectNodes(Arrays.asList(node2));
        pipeline1.setExtractNodes(Arrays.asList(node2));
        pipeline1.setLoadNodes(Arrays.asList(node2));
        channel.setStatus(ChannelStatus.START);
        reflector.invoke(nodeTaskSerivce, "initNodeTask");
        tasks = reflector.invoke(nodeTaskSerivce, "mergeIncNodeTasks");
        want.bool(tasks.size() == 1).is(true);

        // 清理内存
        TestUtils.setField(nodeTaskSerivce, "allTasks", Lists.newArrayList());
        TestUtils.setField(nodeTaskSerivce, "incTasks", Lists.newArrayList());
        channel.setStatus(ChannelStatus.START);
        reflector.invoke(nodeTaskSerivce, "initNodeTask");
        tasks = reflector.invoke(nodeTaskSerivce, "mergeIncNodeTasks");
        want.number(tasks.size()).isEqualTo(1);
        // 删除某个pipeline
        channel.setPipelines(Arrays.asList(pipeline1));
        reflector.invoke(nodeTaskSerivce, "initNodeTask");
        tasks = reflector.invoke(nodeTaskSerivce, "mergeIncNodeTasks");
        want.number(tasks.size()).isEqualTo(1);
    }
}
