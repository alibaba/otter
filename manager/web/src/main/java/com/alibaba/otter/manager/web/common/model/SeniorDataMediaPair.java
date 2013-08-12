package com.alibaba.otter.manager.web.common.model;

import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;

/**
 * @author simon 2012-3-2 下午04:40:29
 */
public class SeniorDataMediaPair {

    private Channel       channel;

    private DataMediaPair dataMediaPair;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public DataMediaPair getDataMediaPair() {
        return dataMediaPair;
    }

    public void setDataMediaPair(DataMediaPair dataMediaPair) {
        this.dataMediaPair = dataMediaPair;
    }

}
