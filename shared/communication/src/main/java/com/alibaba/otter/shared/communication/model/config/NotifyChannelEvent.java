package com.alibaba.otter.shared.communication.model.config;

import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.common.model.config.channel.Channel;

/**
 * config变更通知的事件
 * 
 * @author jianghang
 */
public class NotifyChannelEvent extends Event {

    private static final long serialVersionUID = -8472088519060045661L;

    public NotifyChannelEvent(){
        super(ConfigEventType.notifyChannel);
    }

    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

}
