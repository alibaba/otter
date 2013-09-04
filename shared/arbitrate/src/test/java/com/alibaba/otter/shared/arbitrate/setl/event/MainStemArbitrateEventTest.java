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

package com.alibaba.otter.shared.arbitrate.setl.event;

import java.util.Arrays;

import mockit.Mock;
import mockit.Mockit;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.PipelineArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.MainStemArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * 测试下mainstem信号
 * 
 * @author jianghang 2011-9-22 下午04:58:45
 * @version 4.0.0
 */
public class MainStemArbitrateEventTest extends BaseEventTest {

    private MainStemArbitrateEvent mainStemEvent;
    private ChannelArbitrateEvent  channelEvent;
    private PipelineArbitrateEvent pipelineEvent;
    private final Node             local      = new Node();
    private final Long             nid        = 1L;
    private Long                   channelId  = 100L;
    private Long                   pipelineId = 100L;
    private NodeArbitrateEvent     nodeEvent;

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
                return null;// 没有反向同步
            }

            @Mock
            public int getParallelism(Long pipelineId) {
                return 3;// 并行度
            }

            @Mock
            public Pipeline getPipeline(Long pipelineId) {
                Pipeline pipeline = new Pipeline();
                pipeline.setId(pipelineId);
                pipeline.setSelectNodes(Arrays.asList(local));
                pipeline.setExtractNodes(Arrays.asList(local));
                pipeline.setLoadNodes(Arrays.asList(local));
                return pipeline;
            }

            @Mock
            public Long getCurrentNid() {
                return nid;
            }

        });

        getZookeeper();
        local.setId(nid);
        nodeEvent = new NodeArbitrateEvent();
        channelEvent = new ChannelArbitrateEvent();
        pipelineEvent = new PipelineArbitrateEvent();

        nodeEvent.init(nid);
        channelEvent.init(channelId);
        pipelineEvent.init(channelId, pipelineId);
        channelEvent.start(channelId);
    }

    @Test
    public void test_mainStem() {
        channelEvent.start(channelId);// 启动
        mainStemEvent = new MainStemArbitrateEvent();
        try {
            mainStemEvent.await(pipelineId);

            MainStemEventData eventData = new MainStemEventData();
            eventData.setPipelineId(pipelineId);
            eventData.setStatus(MainStemEventData.Status.OVERTAKE);
            mainStemEvent.single(eventData);

            PermitMonitor permit = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
            permit.waitForPermit();// 阻塞等待授权
        } catch (InterruptedException e) {
            want.fail();
        }

        boolean check = mainStemEvent.check(pipelineId);
        want.bool(check).is(true);

        // 删除mainStem节点
        mainStemEvent.release(pipelineId);
    }

    @AfterClass
    public void destory() {
        nodeEvent.destory(nid);
        pipelineEvent.destory(channelId, pipelineId);
        channelEvent.destory(channelId);
    }
}
