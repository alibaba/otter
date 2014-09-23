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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.manager.biz.config.alarm.AlarmRuleService;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;

public class AlarmSystemList {

    @Resource(name = "alarmRuleService")
    private AlarmRuleService alarmRuleService;

    public void execute(@Param("pipelineId") Long pipelineId, @Param("pageIndex") int pageIndex, Context context)
                                                                                                                 throws Exception {
        Map<String, Object> condition = new HashMap<String, Object>();
        int count = alarmRuleService.getCount();
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());

        List<AlarmRule> alarmRules = alarmRuleService.listAllAlarmRules(condition);
        StringBuffer buffer = new StringBuffer();
        for (AlarmRule alarmRule : alarmRules) {
            buffer.append(alarmRule.getId());
            buffer.append(",");
        }
        context.put("alarmRules", alarmRules);
        context.put("alarmRuleIds", buffer.toString());
        context.put("paginator", paginator);
    }
}
