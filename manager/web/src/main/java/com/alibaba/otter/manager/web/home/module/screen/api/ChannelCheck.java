package com.alibaba.otter.manager.web.home.module.screen.api;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;

/**
 * @author zebin.xuzb @ 2012-5-18
 */
public class ChannelCheck extends AbstractJsonScreen<String> {

    @Resource(name = "channelService")
    private ChannelService channelService;

    public void execute(@Param("id") Long id) {

        try {

            Channel channel = channelService.findById(id);

            returnSuccess(channel.getStatus().toString());

        } catch (Exception e) {
            String errorMsg = String.format("error happens while [check status] channel with id [%d]", id);
            log.error(errorMsg, e);
            returnError(errorMsg);
        }
    }
}
