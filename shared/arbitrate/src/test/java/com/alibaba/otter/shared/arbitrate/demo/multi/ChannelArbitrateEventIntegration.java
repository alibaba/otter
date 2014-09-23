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

package com.alibaba.otter.shared.arbitrate.demo.multi;

import java.util.Arrays;

import mockit.Mock;
import mockit.Mockit;

import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 集成测试
 * 
 * @author jianghang 2011-10-8 下午06:25:52
 * @version 4.0.0
 */
public class ChannelArbitrateEventIntegration extends BaseEventTest {

    @SpringBeanByName
    private ChannelArbitrateEvent channelEvent;

    private ZkClientx             zookeeper          = null;
    private Long                  channelId          = 100L;
    private Long                  pipelineId         = 100L;
    private Long                  oppositePipelineId = 101L;

    @BeforeClass
    public void init() {
        // 初始化节点
        // mock 配置信息数据
        Mockit.setUpMock(ArbitrateConfigUtils.class, new Object() {

            @Mock
            public Pipeline getPipeline(Long pipelineId) {
                Pipeline pipeline = new Pipeline();
                pipeline.setId(pipelineId);
                return pipeline;
            }

            @Mock
            public Channel getChannel(Long pipelineId) {
                Channel channel = new Channel();
                channel.setId(channelId);

                Pipeline pipeline = new Pipeline();
                pipeline.setId(pipelineId);

                Pipeline oppositePipeline = new Pipeline();
                oppositePipeline.setId(oppositePipelineId);
                channel.setPipelines(Arrays.asList(pipeline, oppositePipeline));
                return channel;
            }

            @Mock
            public Channel getChannelByChannelId(Long channelId) {
                Channel channel = new Channel();
                channel.setId(channelId);

                Pipeline pipeline = new Pipeline();
                pipeline.setId(pipelineId);

                Pipeline oppositePipeline = new Pipeline();
                oppositePipeline.setId(oppositePipelineId);
                channel.setPipelines(Arrays.asList(pipeline, oppositePipeline));
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
    }

    @Test
    public void test_aPause() {
        // 启动
        channelEvent.pause(channelId);
        String path = StagePathUtils.getChannelByChannelId(channelId);
        byte[] data = zookeeper.readData(path);
        ChannelStatus status = JsonUtils.unmarshalFromByte(data, ChannelStatus.class);
        want.bool(status == ChannelStatus.PAUSE).is(true);
    }

    // @Test
    // public void test_aStop() {
    // //启动
    // channelEvent.stop(channelId);
    // String path = PathUtils.getChannelByChannelId(channelId);
    // byte[] data = null;
    // try {
    // data = zookeeper.getData(path, false, null);
    // } catch (KeeperException e) {
    // want.fail();
    // } catch (InterruptedException e) {
    // want.fail();
    // }
    // ChannelStatus status = JsonUtils.unmarshalFromByte(data,
    // ChannelStatus.class);
    // want.bool(status == ChannelStatus.STOP).is(true);
    // }

    @Test
    public void test_bStart() {
        // 启动
        channelEvent.start(channelId);
        String path = StagePathUtils.getChannelByChannelId(channelId);
        byte[] data = zookeeper.readData(path);
        ChannelStatus status = JsonUtils.unmarshalFromByte(data, ChannelStatus.class);
        want.bool(status == ChannelStatus.START).is(true);
    }

}
