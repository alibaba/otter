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
