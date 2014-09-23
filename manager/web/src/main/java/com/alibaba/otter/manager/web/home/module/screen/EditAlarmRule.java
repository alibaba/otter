/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
