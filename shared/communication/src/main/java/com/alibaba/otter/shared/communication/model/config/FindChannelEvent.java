package com.alibaba.otter.shared.communication.model.config;

import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 配置查询的事件
 * 
 * @author jianghang
 */
public class FindChannelEvent extends Event {

    private static final long serialVersionUID = 476657754177940448L;

    private Long              channelId;                             // 对应的channelId, 可能为空
    private Long              pipelineId;                            // 对应的pipelineId, 可能为空

    public FindChannelEvent() {
        super(ConfigEventType.findChannel);
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

}
