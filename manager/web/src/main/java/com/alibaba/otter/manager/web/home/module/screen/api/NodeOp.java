package com.alibaba.otter.manager.web.home.module.screen.api;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.remote.NodeRemoteService;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;

public class NodeOp extends AbstractJsonScreen<String> {

    private final static String    ONLINE  = "online";

    private final static String    OFFLINE = "offline";

    private final static String    THREAD  = "thread";
    private final static String    PROFILE = "profile";

    @Resource(name = "channelService")
    private ChannelService         channelService;

    @Resource(name = "arbitrateManageService")
    private ArbitrateManageService arbitrateManageService;

    @Resource(name = "nodeRemoteService")
    private NodeRemoteService      nodeRemoteService;

    public void execute(@Param("nid") Long nid, @Param("command") String command, @Param("value") String value) {
        try {
            if (StringUtils.equalsIgnoreCase(command, OFFLINE)) {
                List<Channel> channels = channelService.listByNodeId(nid, ChannelStatus.START);
                for (Channel channel : channels) {// 重启一下对应的channel
                    boolean result = arbitrateManageService.channelEvent().restart(channel.getId());
                    if (result) {
                        channelService.notifyChannel(channel.getId());// 推送一下配置
                    }
                }
            } else if (StringUtils.equalsIgnoreCase(command, ONLINE)) {
                // doNothing，自动会加入服务列表
            } else if (StringUtils.endsWithIgnoreCase(command, THREAD)) {
                nodeRemoteService.setThreadPoolSize(nid, Integer.valueOf(value));
            } else if (StringUtils.endsWithIgnoreCase(command, PROFILE)) {
                nodeRemoteService.setProfile(nid, BooleanUtils.toBoolean(value));
            } else {
                returnError("please add specfy the 'command' param.");
                return;
            }

            returnSuccess();
        } catch (Exception e) {
            String errorMsg = String.format("error happens while [%s] with node id [%d]", command, nid);
            log.error(errorMsg, e);
            returnError(errorMsg);
        }
    }
}
