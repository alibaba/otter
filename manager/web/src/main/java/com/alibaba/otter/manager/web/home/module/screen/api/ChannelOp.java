package com.alibaba.otter.manager.web.home.module.screen.api;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;

/**
 * @author zebin.xuzb @ 2012-5-18
 */
public class ChannelOp extends AbstractJsonScreen<String> {

    private final static String START = "start";
    private final static String STOP  = "stop";

    @Resource(name = "channelService")
    private ChannelService      channelService;

    public void execute(@Param("id") Long id, @Param("command") String command) {

        try {
            if (StringUtils.equalsIgnoreCase(command, START)) {
                channelService.startChannel(id);
            } else if (StringUtils.equalsIgnoreCase(command, STOP)) {
                channelService.stopChannel(id);
            } else {
                returnError("please add specfy the 'command' param.");
                return;
            }
            returnSuccess();
        } catch (Exception e) {
            String errorMsg = String.format("error happens while [%s] channel with id [%d]", command, id);
            log.error(errorMsg, e);
            returnError(errorMsg);
        }
    }
}
