package com.alibaba.otter.manager.web.home.module.screen;

import java.util.List;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.alarm.AlarmRuleService;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.channel.Channel;

public class AlarmRuleList {

    @Resource(name = "alarmRuleService")
    private AlarmRuleService alarmRuleService;

    @Resource(name = "channelService")
    private ChannelService   channelService;

    public void execute(@Param("pipelineId") Long pipelineId, Context context) throws Exception {

        List<AlarmRule> alarmRules = alarmRuleService.getAlarmRules(pipelineId);
        Channel channel = channelService.findByPipelineId(pipelineId);
        context.put("alarmRules", alarmRules);
        context.put("pipelineId", pipelineId);
        context.put("channelId", channel.getId());
    }
}
