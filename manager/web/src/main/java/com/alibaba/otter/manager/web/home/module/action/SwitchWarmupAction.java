package com.alibaba.otter.manager.web.home.module.action;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;

public class SwitchWarmupAction {

    @Resource(name = "channelService")
    private ChannelService         channelService;

    @Resource(name = "arbitrateManageService")
    private ArbitrateManageService arbitrateManageService;

    public void doSwitch(@Param("pipelineId") Long pipelineId, Navigator nav) throws Exception {
        Channel channel = channelService.findByPipelineId(pipelineId);
        arbitrateManageService.channelEvent().restart(channel.getId());// 尝试重新启动
        arbitrateManageService.systemEvent().switchWarmup(channel.getId(), pipelineId);
        nav.redirectToLocation("analysisStageStat.htm?pipelineId=" + pipelineId);
    }

    public void doRestart(@Param("pipelineId") Long pipelineId, Navigator nav) throws Exception {
        Channel channel = channelService.findByPipelineId(pipelineId);
        arbitrateManageService.channelEvent().restart(channel.getId());// 尝试重新启动
        channelService.notifyChannel(channel.getId());// 推送下配置
        nav.redirectToLocation("analysisStageStat.htm?pipelineId=" + pipelineId);
    }

}
