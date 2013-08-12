package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;

public class ChannelInfo {

    @Resource(name = "channelService")
    private ChannelService channelService;

    public void execute(@Param("channelId") Long channelId, Context context) throws Exception {
        Channel channel = channelService.findById(channelId);

        context.put("channel", channel);
    }
}
