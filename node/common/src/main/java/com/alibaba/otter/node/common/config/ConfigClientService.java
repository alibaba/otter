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

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfig;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * 基于本地内存的config cache实现
 * 
 * @author jianghang
 */
public interface ConfigClientService extends ArbitrateConfig {

    /**
     * 查询当前节点的Node信息
     */
    public Node currentNode();

    /**
     * 根据对应的nid查询Node信息
     */
    public Node findNode(Long nid);

    /**
     * 根据channelId获取channel
     * 
     * @param channelId
     * @return
     */
    public Channel findChannel(Long channelId);

    /**
     * 根据pipelineId获取pipeline
     * 
     * @param pipelineId
     * @return
     */
    public Pipeline findPipeline(Long pipelineId);

    /**
     * 根据pipelineId查询对应的Channel对象
     * 
     * @param pipelineId
     * @return
     */
    public Channel findChannelByPipelineId(Long pipelineId);

    /**
     * 根据pipelineId查询相对的Pipeline对象
     * 
     * @param pipelineId
     * @return
     */
    public Pipeline findOppositePipeline(Long pipelineId);

}
