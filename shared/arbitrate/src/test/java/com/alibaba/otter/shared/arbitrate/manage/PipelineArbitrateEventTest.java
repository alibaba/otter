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

package com.alibaba.otter.shared.arbitrate.manage;

import mockit.Mock;
import mockit.Mockit;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.PipelineArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 测试下pipeline
 * 
 * @author jianghang 2011-9-19 下午01:04:15
 * @version 4.0.0
 */
public class PipelineArbitrateEventTest extends BaseEventTest {

    private ZkClientx              zookeeper  = null;
    private ChannelArbitrateEvent  channelEvent;
    private PipelineArbitrateEvent pipelineEvent;
    private Long                   channelId  = 100L;
    private Long                   pipelineId = 100L;

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

    @Test
    public void testInit() {
        channelEvent.init(channelId);// 一定要先初始化channel
        channelEvent.start(channelId);
        pipelineEvent.init(channelId, pipelineId);// 初始化piepline
        String path = StagePathUtils.getPipeline(channelId, pipelineId);
        want.bool(zookeeper.exists(path)).is(true);

        pipelineEvent.destory(channelId, pipelineId);
        channelEvent.destory(channelId);
    }

    @Test
    public void testDestory() {
        channelEvent.init(channelId);// 一定要先初始化channel
        pipelineEvent.init(channelId, pipelineId);// 初始化piepline
        pipelineEvent.destory(channelId, pipelineId);
        channelEvent.destory(channelId);

        String path = StagePathUtils.getPipeline(channelId, pipelineId);
        want.bool(zookeeper.exists(path)).is(false);
    }

}
