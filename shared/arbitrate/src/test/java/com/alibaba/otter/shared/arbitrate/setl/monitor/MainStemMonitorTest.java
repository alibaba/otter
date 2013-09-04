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

import java.util.Arrays;

import mockit.Mock;
import mockit.Mockit;

import org.I0Itec.zkclient.exception.ZkBadVersionException;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.zookeeper.data.Stat;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.PipelineArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.helper.ManagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.MainstemMonitor;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

public class MainStemMonitorTest extends BaseEventTest {

    private ZkClientx              zookeeper  = null;
    private ChannelArbitrateEvent  channelEvent;
    private PipelineArbitrateEvent pipelineEvent;
    private NodeArbitrateEvent     nodeEvent;
    private final Node             local      = new Node();
    private final Long             nid        = 1L;
    private Long                   channelId  = 100L;
    private Long                   pipelineId = 100L;

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

        zookeeper = getZookeeper();
        local.setId(nid);
        nodeEvent = new NodeArbitrateEvent();
        channelEvent = new ChannelArbitrateEvent();
        pipelineEvent = new PipelineArbitrateEvent();
    }

    @BeforeMethod
    public void setUp() {
        nodeEvent.init(nid);
        channelEvent.init(channelId);
        pipelineEvent.init(channelId, pipelineId);
        channelEvent.start(channelId);
    }

    @AfterMethod
    public void tearDown() {
        nodeEvent.destory(nid);
        pipelineEvent.destory(channelId, pipelineId);
        channelEvent.destory(channelId);

    }

    @Test
    public void testInit() {
        MainstemMonitor mainstemMonitor = ArbitrateFactory.getInstance(pipelineId, MainstemMonitor.class);
        boolean running = mainstemMonitor.check();
        want.bool(running).is(true);

        try {
            mainstemMonitor.waitForActive();
        } catch (InterruptedException e) {
            want.fail();
        }

        ArbitrateFactory.destory(pipelineId);
        mainstemMonitor.releaseMainstem();
    }

    @Test
    public void testRelease() {
        MainstemMonitor mainstemMonitor = ArbitrateFactory.getInstance(pipelineId, MainstemMonitor.class);
        boolean running = mainstemMonitor.check();
        want.bool(running).is(true);

        boolean release = mainstemMonitor.releaseMainstem();// 模拟一次断网，
        want.bool(release).is(true);

        long start = System.currentTimeMillis();
        try {
            mainstemMonitor.waitForActive();
        } catch (InterruptedException e) {
            want.fail();
        }
        want.number(System.currentTimeMillis() - start).isLe(1000L);
        ArbitrateFactory.destory(pipelineId);
        mainstemMonitor.releaseMainstem();
    }

    @Test
    public void testManualRelease() {
        MainstemMonitor mainstemMonitor = ArbitrateFactory.getInstance(pipelineId, MainstemMonitor.class);
        boolean running = mainstemMonitor.check();
        want.bool(running).is(true);
        mainstemMonitor.setDelayTime(5);

        switchWarmup(channelId, pipelineId);
        sleep(1000L); // 等manual release被响应

        long start = System.currentTimeMillis();
        try {
            mainstemMonitor.waitForActive();
        } catch (InterruptedException e) {
            want.fail();
        }
        want.number(System.currentTimeMillis() - start).isGe(4000L);
        ArbitrateFactory.destory(pipelineId);
        mainstemMonitor.releaseMainstem();
    }

    /**
     * 手工触发一次主备切换
     */
    private void switchWarmup(Long channelId, Long pipelineId) {
        String path = ManagePathUtils.getMainStem(channelId, pipelineId);
        try {
            while (true) {
                Stat stat = new Stat();
                byte[] bytes = zookeeper.readData(path, stat);
                MainStemEventData mainStemData = JsonUtils.unmarshalFromByte(bytes, MainStemEventData.class);
                mainStemData.setActive(false);
                try {
                    zookeeper.writeData(path, JsonUtils.marshalToByte(mainStemData), stat.getVersion());
                    break;
                } catch (ZkBadVersionException e) {
                    // ignore , retrying
                }

            }
        } catch (ZkNoNodeException e) {
            // ignore
        } catch (ZkException e) {
            throw new ArbitrateException("releaseMainStem", pipelineId.toString(), e);
        }
    }

}
