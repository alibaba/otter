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

package com.alibaba.otter.manager.biz.common.arbitrate;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfig;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigRegistry;
import com.alibaba.otter.shared.common.model.config.ConfigException;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.cache.RefreshMemoryMirror;
import com.alibaba.otter.shared.common.utils.cache.RefreshMemoryMirror.ComputeFunction;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * manager下的基于db查询的{@linkplain ArbitrateConfig}实现
 * 
 * @author jianghang 2011-11-3 上午11:09:24
 * @version 4.0.0
 */
public class ArbitrateConfigImpl implements ArbitrateConfig, InitializingBean {

    private static final Long                  DEFAULT_PERIOD = 60 * 1000L;
    private Long                               timeout        = DEFAULT_PERIOD;
    private RefreshMemoryMirror<Long, Channel> channelCache;
    private Map<Long, Long>                    channelMapping;
    private ChannelService                     channelService;
    private NodeService                        nodeService;
    private RefreshMemoryMirror<Long, Node>    nodeCache;

    public ArbitrateConfigImpl(){
        // 注册自己到arbitrate模块
        ArbitrateConfigRegistry.regist(this);
    }

    public Node currentNode() {
        return null;
    }

    public Node findNode(Long nid) {
        return nodeCache.get(nid);
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

    public void afterPropertiesSet() throws Exception {
        // 获取一下nid变量
        channelMapping = new MapMaker().makeComputingMap(new Function<Long, Long>() {

            public Long apply(Long pipelineId) {
                // 处理下pipline -> channel映射关系不存在的情况
                Channel channel = channelService.findByPipelineId(pipelineId);
                if (channel == null) {
                    throw new ConfigException("No Such Channel by pipelineId[" + pipelineId + "]");
                }

                updateMapping(channel, pipelineId);// 排除下自己
                channelCache.put(channel.getId(), channel);// 更新下channelCache
                return channel.getId();

            }
        });

        channelCache = new RefreshMemoryMirror<Long, Channel>(timeout, new ComputeFunction<Long, Channel>() {

            public Channel apply(Long key, Channel oldValue) {
                Channel channel = channelService.findById(key);
                if (channel == null) {
                    // 其他情况直接返回内存中的旧值
                    return oldValue;
                } else {
                    updateMapping(channel, null);// 排除下自己
                    return channel;
                }
            }
        });

        nodeCache = new RefreshMemoryMirror<Long, Node>(timeout, new ComputeFunction<Long, Node>() {

            public Node apply(Long key, Node oldValue) {
                Node node = nodeService.findById(key);
                if (node == null) {
                    return oldValue;
                } else {
                    return node;
                }
            }
        });
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

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

}
