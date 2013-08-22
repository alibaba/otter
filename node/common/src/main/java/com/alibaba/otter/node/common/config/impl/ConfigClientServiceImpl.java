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

package com.alibaba.otter.node.common.config.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.node.common.communication.NodeCommmunicationClient;
import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfig;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigRegistry;
import com.alibaba.otter.shared.common.model.config.ConfigException;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.cache.RefreshMemoryMirror;
import com.alibaba.otter.shared.common.utils.cache.RefreshMemoryMirror.ComputeFunction;
import com.alibaba.otter.shared.communication.model.config.FindChannelEvent;
import com.alibaba.otter.shared.communication.model.config.FindNodeEvent;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * task节点对应的config对象管理服务
 * 
 * @author jianghang
 */
public class ConfigClientServiceImpl implements InternalConfigClientService, ArbitrateConfig, InitializingBean {

    private static final String                NID_NAME       = "nid";
    private static final Long                  DEFAULT_PERIOD = 60 * 1000L;
    private static final Logger                logger         = LoggerFactory.getLogger(ConfigClientService.class);

    private Long                               timeout        = DEFAULT_PERIOD;
    private Long                               nid;
    private NodeCommmunicationClient           nodeCommmunicationClient;
    private RefreshMemoryMirror<Long, Channel> channelCache;
    private Map<Long, Long>                    channelMapping;                                                     // 将pipelineId映射为channelId
    private RefreshMemoryMirror<Long, Node>    nodeCache;

    public ConfigClientServiceImpl(){
        // 注册一下事件处理
        ArbitrateConfigRegistry.regist(this);
    }

    public Node currentNode() {
        Node node = nodeCache.get(nid);
        if (node == null) {
            throw new ConfigException("nid:" + nid + " in manager[" + nodeCommmunicationClient.getManagerAddress()
                                      + "]is not found!");
        }

        return node;
    }

    public Channel findChannel(Long channelId) {
        return channelCache.get(channelId);
    }

    public Channel findChannelByPipelineId(Long pipelineId) {
        Long channelId = channelMapping.get(pipelineId);
        return channelCache.get(channelId);
    }

    public Pipeline findOppositePipeline(Long pipelineId) {
        Long channelId = channelMapping.get(pipelineId);
        Channel channel = channelCache.get(channelId);
        List<Pipeline> pipelines = channel.getPipelines();
        for (Pipeline pipeline : pipelines) {
            if (pipeline.getId().equals(pipelineId) == false) {// 这里假定pipeline只有两个
                return pipeline;
            }
        }

        return null;
    }

    public Pipeline findPipeline(Long pipelineId) {
        Long channelId = channelMapping.get(pipelineId);
        Channel channel = channelCache.get(channelId);
        List<Pipeline> pipelines = channel.getPipelines();
        for (Pipeline pipeline : pipelines) {
            if (pipeline.getId().equals(pipelineId)) {
                return pipeline;
            }
        }

        throw new ConfigException("no pipeline for pipelineId[" + pipelineId + "]");
    }

    public Node findNode(Long nid) {
        return nodeCache.get(nid);
    }

    public void afterPropertiesSet() throws Exception {
        // 获取一下nid变量
        String nid = System.getProperty(NID_NAME);
        if (StringUtils.isEmpty(nid)) {
            throw new ConfigException("nid is not set!");
        }

        this.nid = Long.valueOf(nid);

        channelMapping = new MapMaker().makeComputingMap(new Function<Long, Long>() {

            public Long apply(Long pipelineId) {
                // 处理下pipline -> channel映射关系不存在的情况
                FindChannelEvent event = new FindChannelEvent();
                event.setPipelineId(pipelineId);
                try {
                    Object obj = nodeCommmunicationClient.callManager(event);
                    if (obj != null && obj instanceof Channel) {
                        Channel channel = (Channel) obj;
                        updateMapping(channel, pipelineId);// 排除下自己
                        channelCache.put(channel.getId(), channel);// 更新下channelCache
                        return channel.getId();
                    }
                } catch (Exception e) {
                    logger.error("call_manager_error", event.toString(), e);
                }

                throw new ConfigException("No Such Channel by pipelineId[" + pipelineId + "]");
            }
        });

        nodeCache = new RefreshMemoryMirror<Long, Node>(timeout, new ComputeFunction<Long, Node>() {

            public Node apply(Long key, Node oldValue) {
                FindNodeEvent event = new FindNodeEvent();
                event.setNid(key);
                try {
                    Object obj = nodeCommmunicationClient.callManager(event);
                    if (obj != null && obj instanceof Node) {
                        return (Node) obj;
                    } else {
                        throw new ConfigException("No Such Node by id[" + key + "]");
                    }
                } catch (Exception e) {
                    logger.error("call_manager_error", event.toString(), e);
                }
                // 其他情况直接返回内存中的旧值
                return oldValue;
            }
        });

        channelCache = new RefreshMemoryMirror<Long, Channel>(timeout, new ComputeFunction<Long, Channel>() {

            public Channel apply(Long key, Channel oldValue) {
                FindChannelEvent event = new FindChannelEvent();
                event.setChannelId(key);
                try {
                    Object obj = nodeCommmunicationClient.callManager(event);
                    if (obj != null && obj instanceof Channel) {
                        updateMapping((Channel) obj, null);// 排除下自己
                        return (Channel) obj;
                    } else {
                        throw new ConfigException("No Such Channel by pipelineId[" + key + "]");
                    }
                } catch (Exception e) {
                    logger.error("call_manager_error", event.toString(), e);
                }
                // 其他情况直接返回内存中的旧值
                return oldValue;
            }
        });
    }

    public void createOrUpdateChannel(Channel channel) {
        channelCache.put(channel.getId(), channel);
        updateMapping(channel, null);
    }

    private void updateMapping(Channel channel, Long excludeId) {
        Long channelId = channel.getId();
        List<Pipeline> pipelines = channel.getPipelines();
        for (Pipeline pipeline : pipelines) {
            if (excludeId == null || !pipeline.getId().equals(excludeId)) {
                channelMapping.put(pipeline.getId(), channelId);
            }
        }
    }

    // =================== setter / getter ======================

    public void setNodeCommmunicationClient(NodeCommmunicationClient nodeCommmunicationClient) {
        this.nodeCommmunicationClient = nodeCommmunicationClient;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

}
