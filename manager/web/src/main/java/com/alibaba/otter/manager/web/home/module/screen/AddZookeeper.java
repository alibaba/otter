package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;

public class AddZookeeper {

    @Resource(name = "channelService")
    private ChannelService channelService;

    public void execute(@Param("message") String message, Context context, Navigator nav) throws Exception {
        context.put("message", message);
    }
}
