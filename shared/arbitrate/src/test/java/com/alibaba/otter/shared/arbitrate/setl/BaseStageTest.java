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

package com.alibaba.otter.shared.arbitrate.setl;

import java.util.Arrays;
import java.util.Map;

import mockit.Mock;
import mockit.Mockit;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.communication.ArbitrateCommmunicationClient;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.PipelineArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.node.NodeStatus;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.TestUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;
import com.alibaba.otter.shared.communication.core.model.Callback;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 测试基类
 * 
 * @author jianghang 2011-9-21 下午08:21:21
 * @version 4.0.0
 */
public class BaseStageTest extends BaseEventTest {

    protected ZkClientx              zookeeper  = null;
    // 环境数据准备对象
    protected NodeArbitrateEvent     nodeEvent;
    protected ChannelArbitrateEvent  channelEvent;
    protected PipelineArbitrateEvent pipelineEvent;
    protected final Node             local      = new Node();
    protected final Long             nid        = 1L;
    protected Long                   channelId  = 100L;
    protected Long                   pipelineId = 100L;
    protected String                 pipelinePath;
    protected String                 processPath;

    @BeforeClass
    public void init() {
        // 初始化节点
        // mock 配置信息数据
        local.setStatus(NodeStatus.START);
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
                channel.setPipelines(Arrays.asList(pipeline));
                return channel;
            }

            @Mock
            public Channel getChannel(Long pipelineId) {
                Channel channel = new Channel();
                channel.setId(channelId);
                Pipeline pipeline = new Pipeline();
                pipeline.setId(pipelineId);
                pipeline.setSelectNodes(Arrays.asList(local));
                pipeline.setExtractNodes(Arrays.asList(local));
                pipeline.setLoadNodes(Arrays.asList(local));
                channel.setPipelines(Arrays.asList(pipeline));
                return channel;
            }

            @Mock
            public Pipeline getPipeline(Long pipelineId) {
                Pipeline pipeline = new Pipeline();
                pipeline.setSelectNodes(Arrays.asList(local));
                pipeline.setExtractNodes(Arrays.asList(local));
                pipeline.setLoadNodes(Arrays.asList(local));
                return pipeline;
            }

            @Mock
            public Pipeline getOppositePipeline(Long pipelineId) {
                return null;// 没有反向同步
            }

            @Mock
            public Long getCurrentNid() {
                return nid;
            }

            @Mock
            public int getParallelism(Long pipelineId) {
                return 3;// 并行度
            }

        });

        Mockit.setUpMock(ArbitrateCommmunicationClient.class, new Object() {

            @Mock
            public Object callManager(final Event event) {
                // do nothing
                return null;
            }

            @Mock
            public void callManager(final Event event, final Callback callback) {
                // do nothing
            }
        });

        zookeeper = getZookeeper();
        local.setId(nid);
        nodeEvent = new NodeArbitrateEvent();
        channelEvent = new ChannelArbitrateEvent();
        pipelineEvent = new PipelineArbitrateEvent();

        pipelinePath = StagePathUtils.getPipeline(channelId, pipelineId);
        processPath = StagePathUtils.getProcessRoot(channelId, pipelineId);
        channelEvent.init(channelId);
        pipelineEvent.init(channelId, pipelineId);
        channelEvent.start(channelId);

        String path = pipelinePath + "/" + ArbitrateConstants.NODE_MAINSTEM;
        MainStemEventData eventData = new MainStemEventData();
        eventData.setStatus(MainStemEventData.Status.OVERTAKE);
        eventData.setNid(nid);
        byte[] bytes = JsonUtils.marshalToByte(eventData);// 初始化的数据对象

        zookeeper.create(path, bytes, CreateMode.EPHEMERAL);
    }

    @AfterClass
    public void destory() {
        ArbitrateFactory.destory(pipelineId);
        // 删除mainStem节点
        String path = pipelinePath + "/" + ArbitrateConstants.NODE_MAINSTEM;

        zookeeper.delete(path);

        pipelineEvent.destory(channelId, pipelineId);
        channelEvent.destory(channelId);
    }

    @BeforeMethod
    public void setUp() {
        try {
            Map cache = (Map) TestUtils.getField(new ArbitrateFactory(), "cache");
            cache.clear();
        } catch (Exception e) {
            want.fail();
        }
    }

    @AfterMethod
    public void dispose() {
        ArbitrateFactory.destory(pipelineId);
    }

    protected EtlEventData getData(Long nid) {
        EtlEventData data = new EtlEventData();
        data.setNextNid(nid);
        return data;
    }

    // ================================ helper method ================================

    // 模拟创建一个process
    protected Long initProcess() {
        String path = zookeeper.create(processPath + "/", new byte[0], CreateMode.PERSISTENT_SEQUENTIAL);

        // 创建为顺序的节点
        String processNode = StringUtils.substringAfterLast(path, "/");
        return StagePathUtils.getProcessId(processNode);// 添加到当前的process列表
    }

    // 模拟销毁一个process
    protected void destoryProcess(Long processId) {
        String path = processPath + "/" + StagePathUtils.getProcessNode(processId);
        zookeeper.delete(path);
    }

    // 模拟创建一个stage节点
    protected void initStage(Long processId, String stageNode) {
        String path = processPath + "/" + StagePathUtils.getProcessNode(processId) + "/" + stageNode;
        zookeeper.create(path, new byte[0], CreateMode.PERSISTENT);
    }

    // 模拟销毁一个stage节点
    protected void destoryStage(Long processId, String stageNode) {
        String path = processPath + "/" + StagePathUtils.getProcessNode(processId) + "/" + stageNode;

        zookeeper.delete(path);
    }

    // 模拟创建一个带数据的stage节点
    protected void initStage(Long processId, String stageNode, EtlEventData event) {
        String path = processPath + "/" + StagePathUtils.getProcessNode(processId) + "/" + stageNode;

        byte[] datas = JsonUtils.marshalToByte(event);
        zookeeper.create(path, datas, CreateMode.PERSISTENT);
    }

    // 模拟创建一个终结节点
    protected void initTermin(Long processId) {
        TerminEventData data = new TerminEventData();
        data.setPipelineId(pipelineId);
        data.setProcessId(processId);
        data.setType(TerminType.NORMAL);

        byte[] bytes = JsonUtils.marshalToByte(data);
        zookeeper.create(StagePathUtils.getTermin(pipelineId, processId), bytes, CreateMode.PERSISTENT);

    }

    // 模拟删除一个终结节点
    protected void destoryTermin(Long processId) {
        zookeeper.delete(StagePathUtils.getTermin(pipelineId, processId));

    }

}
