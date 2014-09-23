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

package com.alibaba.otter.manager.biz.remote.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.datamatrix.DataMatrixService;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.remote.ConfigRemoteService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.data.DataMatrix;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.communication.core.CommunicationClient;
import com.alibaba.otter.shared.communication.core.CommunicationRegistry;
import com.alibaba.otter.shared.communication.model.config.ConfigEventType;
import com.alibaba.otter.shared.communication.model.config.FindChannelEvent;
import com.alibaba.otter.shared.communication.model.config.FindMediaEvent;
import com.alibaba.otter.shared.communication.model.config.FindNodeEvent;
import com.alibaba.otter.shared.communication.model.config.FindTaskEvent;
import com.alibaba.otter.shared.communication.model.config.NotifyChannelEvent;

/**
 * Config的remote接口处理
 * 
 * @author jianghang 2011-10-21 下午02:53:53
 * @version 4.0.0
 */
public class ConfigRemoteServiceImpl implements ConfigRemoteService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigRemoteServiceImpl.class);
    private CommunicationClient communicationClient;
    private ChannelService      channelService;
    private NodeService         nodeService;
    private DataMatrixService   dataMatrixService;

    public ConfigRemoteServiceImpl(){
        // 注册一下事件处理
        CommunicationRegistry.regist(ConfigEventType.findChannel, this);
        CommunicationRegistry.regist(ConfigEventType.findNode, this);
        CommunicationRegistry.regist(ConfigEventType.findTask, this);
        CommunicationRegistry.regist(ConfigEventType.findMedia, this);
    }

    public boolean notifyChannel(final Channel channel) {
        Assert.notNull(channel);
        // 获取所有的Node节点
        NotifyChannelEvent event = new NotifyChannelEvent();
        event.setChannel(channel);

        Set<String> addrsSet = new HashSet<String>();

        // 组装当前otter所有的存活的node节点
        // List<Node> nodes = nodeService.listAll();
        // for (Node node : nodes) {
        // if (node.getStatus().isStart() &&
        // StringUtils.isNotEmpty(node.getIp()) && node.getPort() != 0) {
        // final String addr = node.getIp() + ":" + node.getPort();
        // addrsList.add(addr);
        // }
        // }

        // 组装当前pipeline下的存活的node节点
        for (Pipeline pipeline : channel.getPipelines()) {
            List<Node> nodes = new ArrayList<Node>();
            nodes.addAll(pipeline.getSelectNodes());
            nodes.addAll(pipeline.getExtractNodes());
            nodes.addAll(pipeline.getLoadNodes());
            for (Node node : nodes) {
                if (node.getStatus().isStart() && StringUtils.isNotEmpty(node.getIp()) && node.getPort() != 0) {
                    String addr = node.getIp() + ":" + node.getPort();
                    if (node.getParameters().getUseExternalIp()) {
                        addr = node.getParameters().getExternalIp() + ":" + node.getPort();
                    }
                    addrsSet.add(addr);
                }
            }
        }

        List<String> addrsList = new ArrayList<String>(addrsSet);
        if (CollectionUtils.isEmpty(addrsList) && channel.getStatus().isStart()) {
            throw new ManagerException("no live node for notifyChannel");
        } else if (CollectionUtils.isEmpty(addrsList)) {
            // 针对关闭操作，可直接处理
            return true;
        } else {
            Collections.shuffle(addrsList);// 做一下随机，避免每次选择的机器都是同一台
            try {
                String[] addrs = addrsList.toArray(new String[addrsList.size()]);
                List<Boolean> result = (List<Boolean>) communicationClient.call(addrs, event); // 推送配置
                logger.info("## notifyChannel to [{}] channel[{}] result[{}]",
                    new Object[] { ArrayUtils.toString(addrs), channel.toString(), result });

                boolean flag = true;
                for (Boolean f : result) {
                    flag &= f;
                }

                return flag;
            } catch (Exception e) {
                logger.error("## notifyChannel error!", e);
                throw new ManagerException(e);
            }
        }
    }

    /**
     * 根据对应的工作节点机器id，获取相关的channel任务
     */
    public Channel onFindChannel(FindChannelEvent event) {
        Assert.notNull(event);
        Long channelId = event.getChannelId();
        Long pipelineId = event.getPipelineId();
        Channel channel = null;
        if (channelId != null) {
            channel = channelService.findById(channelId);
        } else {
            Assert.notNull(pipelineId);
            channel = channelService.findByPipelineId(pipelineId);
        }

        return channel;
    }

    public Node onFindNode(FindNodeEvent event) {
        Assert.notNull(event);
        Assert.notNull(event.getNid());
        return nodeService.findById(event.getNid());
    }

    public List<Channel> onFindTask(FindTaskEvent event) {
        Assert.notNull(event);
        Assert.notNull(event.getNid());
        // 同时查询start/pause状态的同步任务，因为在发布时重启jvm刚好在执行stopNode的restart指令，此时channel处于pause状态，丢失了任务命令
        return channelService.listByNodeId(event.getNid(), ChannelStatus.START, ChannelStatus.PAUSE);
    }

    public String onFindMedia(FindMediaEvent event) {
        Assert.notNull(event);
        Assert.notNull(event.getDataId());
        DataMatrix matrix = dataMatrixService.findByGroupKey(event.getDataId());
        return JsonUtils.marshalToString(matrix);
    }

    // =============== setter / getter ===================

    public void setCommunicationClient(CommunicationClient communicationClient) {
        this.communicationClient = communicationClient;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDataMatrixService(DataMatrixService dataMatrixService) {
        this.dataMatrixService = dataMatrixService;
    }

}
