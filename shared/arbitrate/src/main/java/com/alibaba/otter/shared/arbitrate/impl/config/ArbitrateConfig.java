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

package com.alibaba.otter.shared.arbitrate.impl.config;

import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * 仲裁器相关配置信息
 * 
 * @author jianghang 2011-10-9 上午11:31:41
 * @version 4.0.0
 */
public interface ArbitrateConfig {

    /**
     * 获取当前节点信息
     */
    public Node currentNode();

    /**
     * 根据nid查询Node信息
     */
    public Node findNode(Long nid);

    /**
     * 根据channelId获取channel
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
