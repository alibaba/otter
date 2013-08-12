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
