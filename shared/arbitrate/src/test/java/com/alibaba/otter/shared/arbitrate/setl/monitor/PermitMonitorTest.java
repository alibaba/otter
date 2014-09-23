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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mockit.Mock;
import mockit.Mockit;

import org.apache.zookeeper.CreateMode;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.PipelineArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.MonitorScheduler;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * permit测试
 * 
 * @author jianghang 2011-9-19 下午02:57:14
 * @version 4.0.0
 */
public class PermitMonitorTest extends BaseEventTest {

    private ZkClientx              zookeeper          = null;
    private ChannelArbitrateEvent  channelEvent;
    private PipelineArbitrateEvent pipelineEvent;

    private Long                   channelId          = 100L;
    private Long                   pipelineId         = 100L;
    private Long                   oppositePipelineId = 101L;
    private PermitMonitor          permitMonitor      = null;

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
                pipeline.setId(oppositePipelineId);
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
        pipelineEvent.init(channelId, oppositePipelineId);

        initMainStem(pipelineId);
        initMainStem(oppositePipelineId);
    }

    @AfterMethod
    public void tearDown() {
        destoryMainStem(pipelineId);
        destoryMainStem(oppositePipelineId);
        permitMonitor.destory();
        // MonitorScheduler.unRegister(permitMonitor);
        // 初始化两个pipeline
        pipelineEvent.destory(channelId, pipelineId);
        pipelineEvent.destory(channelId, oppositePipelineId);
        channelEvent.destory(channelId);

    }

    private void initMainStem(Long pipelineId) {
        String pipelinePath = StagePathUtils.getPipeline(channelId, pipelineId);
        String path = pipelinePath + "/" + ArbitrateConstants.NODE_MAINSTEM;

        MainStemEventData eventData = new MainStemEventData();
        eventData.setStatus(MainStemEventData.Status.TAKEING);
        byte[] bytes = JsonUtils.marshalToByte(eventData);// 初始化的数据对象

        zookeeper.create(path, bytes, CreateMode.EPHEMERAL);
    }

    private void updateMainStem(Long pipelineId, MainStemEventData.Status status) {
        String pipelinePath = StagePathUtils.getPipeline(channelId, pipelineId);
        String path = pipelinePath + "/" + ArbitrateConstants.NODE_MAINSTEM;
        MainStemEventData eventData = new MainStemEventData();
        eventData.setStatus(status);
        byte[] bytes = JsonUtils.marshalToByte(eventData);// 初始化的数据对象

        zookeeper.writeData(path, bytes);
    }

    private void destoryMainStem(Long pipelineId) {
        String pipelinePath = StagePathUtils.getPipeline(channelId, pipelineId);
        String path = pipelinePath + "/" + ArbitrateConstants.NODE_MAINSTEM;

        zookeeper.delete(path);
    }

    @Test
    public void testPermit_init_ok() {// 测试下permit的初始化内容
        channelEvent.start(channelId);
        updateMainStem(pipelineId, MainStemEventData.Status.OVERTAKE);
        updateMainStem(oppositePipelineId, MainStemEventData.Status.OVERTAKE);

        permitMonitor = new PermitMonitor(pipelineId);
        boolean isPermit = permitMonitor.isPermit();
        want.bool(isPermit).is(true);
    }

    @Test
    public void testPermit_init_fail() {// 测试下permit的初始化内容
        channelEvent.start(channelId);
        updateMainStem(pipelineId, MainStemEventData.Status.OVERTAKE);
        updateMainStem(oppositePipelineId, MainStemEventData.Status.TAKEING);// 一个节点挂起

        permitMonitor = new PermitMonitor(pipelineId);
        boolean isPermit = permitMonitor.isPermit();
        want.bool(isPermit).is(false);
    }

    @Test
    public void testPermit_change() {
        channelEvent.start(channelId);
        updateMainStem(pipelineId, MainStemEventData.Status.OVERTAKE);
        updateMainStem(oppositePipelineId, MainStemEventData.Status.TAKEING);// 一个节点挂起

        permitMonitor = new PermitMonitor(pipelineId);
        boolean isPermit = permitMonitor.isPermit();
        want.bool(isPermit).is(false);

        updateMainStem(oppositePipelineId, MainStemEventData.Status.OVERTAKE);
        updateMainStem(pipelineId, MainStemEventData.Status.TAKEING);// 一个节点挂起
        sleep();// 需要sleep一下，保证数据已经更新到monitor上
        isPermit = permitMonitor.isPermit();
        want.bool(isPermit).is(false);

        updateMainStem(pipelineId, MainStemEventData.Status.OVERTAKE);
        sleep();// 需要sleep一下，保证数据已经更新到monitor上
        isPermit = permitMonitor.isPermit();
        want.bool(isPermit).is(true);
        MonitorScheduler.unRegister(permitMonitor);
    }

    @Test
    public void testPermit_wait() {
        channelEvent.start(channelId);
        updateMainStem(pipelineId, MainStemEventData.Status.OVERTAKE);
        updateMainStem(oppositePipelineId, MainStemEventData.Status.OVERTAKE);
        permitMonitor = new PermitMonitor(pipelineId);
        boolean isPermit = permitMonitor.isPermit();
        want.bool(isPermit).is(true);
        try {
            permitMonitor.waitForPermit();// 当前为permit=true,立马返回
        } catch (InterruptedException e) {
            want.fail();
        }

        updateMainStem(pipelineId, MainStemEventData.Status.TAKEING);// 一个节点挂起
        sleep();
        isPermit = permitMonitor.isPermit();
        want.bool(isPermit).is(false);
        // 提交一个异步更新状态任务
        final CountDownLatch count = new CountDownLatch(1);
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new Runnable() {

            public void run() {
                sleep();// sleep一下后再触发
                updateMainStem(pipelineId, MainStemEventData.Status.OVERTAKE);// 一个节点挂起
                count.countDown();
            }
        });

        try {
            permitMonitor.waitForPermit();// 当前为permit=false,阻塞，等待信号
        } catch (InterruptedException e) {
            want.fail();
        }

        try {
            count.await();
            executor.shutdown();
        } catch (InterruptedException e) {
            want.fail();
        }

    }

}
