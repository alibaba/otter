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

import mockit.Mock;
import mockit.Mockit;

import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.BaseOtterTest;
import com.alibaba.otter.node.common.communication.NodeCommmunicationClient;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.communication.model.config.FindNodeEvent;

/**
 * config client测试
 * 
 * @author jianghang 2011-10-21 上午10:33:51
 * @version 4.0.0
 */
public class ConfigClientServiceTest extends BaseOtterTest {

    @SpringBeanByName
    private ConfigClientService configClientService;

    @BeforeClass
    public void initial() {
        System.setProperty("nid", "1");
    }

    @Test
    public void test_node() {
        final Node node = new Node();
        Mockit.setUpMock(NodeCommmunicationClient.class, new Object() {

            @Mock
            public Object callManager(final Event event) {
                if (event instanceof FindNodeEvent) {
                    Long nid = ((FindNodeEvent) event).getNid();
                    node.setId(nid);
                }

                return node;
            }
        });

        Node cnode = configClientService.currentNode();
        want.bool(cnode.getId() == 1L);
        Node fnode = configClientService.findNode(2L);
        want.bool(fnode.getId() == 2L);
    }

    @Test
    public void test_pipeline() {
        Long channelId = 100L;
        Long pipelineId = 100L;
        Long oppositePipelineId = 101L;
        final Channel channel = new Channel();
        channel.setId(channelId);
        Pipeline pipeline1 = new Pipeline();
        pipeline1.setChannelId(channelId);
        pipeline1.setId(pipelineId);

        Pipeline pipeline2 = new Pipeline();
        pipeline2.setChannelId(channelId);
        pipeline2.setId(oppositePipelineId);
        channel.setPipelines(Arrays.asList(pipeline1, pipeline2));

        Mockit.setUpMock(NodeCommmunicationClient.class, new Object() {

            @Mock
            public Object callManager(final Event event) {
                return channel;
            }
        });

        Pipeline pipeline = configClientService.findPipeline(pipelineId);
        want.bool(pipeline.getId() == pipelineId);

        pipeline = configClientService.findOppositePipeline(pipelineId);
        want.bool(pipeline.getId() == oppositePipelineId);

        Channel channel1 = configClientService.findChannel(channelId);
        want.bool(channel1.getId() == channelId);

        channel1 = configClientService.findChannelByPipelineId(pipelineId);
        want.bool(channel1.getId() == channelId);
    }
}
