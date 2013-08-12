package com.alibaba.otter.shared.communication.model.arbitrate;

import com.alibaba.otter.shared.communication.core.model.Event;

public class StopChannelEvent extends Event {

    private static final long serialVersionUID = 476657754177940448L;

    private Long              channelId;                             // 对应的channelId

    public StopChannelEvent() {
        super(ArbitrateEventType.stopChannel);
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

}
