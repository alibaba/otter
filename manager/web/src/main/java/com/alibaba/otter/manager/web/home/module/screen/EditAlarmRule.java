package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.alarm.AlarmRuleService;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;

public class EditAlarmRule {

    @Resource(name = "alarmRuleService")
    private AlarmRuleService alarmRuleService;

    @Resource(name = "channelService")
    private ChannelService   channelService;

    public void execute(@Param("alarmRuleId") Long alarmRuleId, Context context, Navigator nav) throws Exception {
        AlarmRule alarmRule = alarmRuleService.getAlarmRuleById(alarmRuleId);

        context.put("alarmRule", alarmRule);
        context.put("channelId", channelService.findByPipelineId(alarmRule.getPipelineId()).getId());
    }

}
