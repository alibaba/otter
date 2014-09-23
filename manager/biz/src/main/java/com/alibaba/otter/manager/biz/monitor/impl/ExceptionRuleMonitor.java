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

package com.alibaba.otter.manager.biz.monitor.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.manager.biz.config.alarm.AlarmRuleService;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRuleStatus;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.alibaba.otter.shared.communication.model.arbitrate.NodeAlarmEvent;

/**
 * @author zebin.xuzb @ 2012-8-29
 * @version 4.1.0
 */
public class ExceptionRuleMonitor extends AbstractRuleMonitor {

    private static final String MESAGE_FORMAT = "pid:%s nid:%s exception:%s";

    @Resource(name = "alarmRuleService")
    private AlarmRuleService    alarmRuleService;

    // ExceptionRuleMonitor(){
    // MonitorRuleExplorerRegisty.register(MonitorName.EXCEPTON, this);
    // }

    @Override
    public void explore(List<AlarmRule> rules) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void feed(Object data, Long pipelineId) {
        if (!(data instanceof NodeAlarmEvent)) {
            return;
        }
        NodeAlarmEvent alarmEvent = (NodeAlarmEvent) data;
        // 异常一定需要记录日志
        String message = String.format(MESAGE_FORMAT, alarmEvent.getPipelineId(), alarmEvent.getNid(),
                                       alarmEvent.getMessage());
        logRecordAlarm(pipelineId, alarmEvent.getNid(), MonitorName.EXCEPTION, message);
        // 报警检查
        List<AlarmRule> rules = alarmRuleService.getAlarmRules(pipelineId, AlarmRuleStatus.ENABLE);

        // TODO 需要给 alarmRuleService 提需求
        Date now = new Date();
        List<AlarmRule> exceptionRules = new ArrayList<AlarmRule>();
        for (AlarmRule rule : rules) {
            if (MonitorName.EXCEPTION.equals(rule.getMonitorName()) && checkEnable(rule, now)) {
                exceptionRules.add(rule);
            }
        }

        if (CollectionUtils.isEmpty(exceptionRules)) {
            return;
        }

        for (AlarmRule rule : exceptionRules) {
            check(rule, alarmEvent);
        }
    }

    private boolean checkEnable(AlarmRule rule, Date now) {
        return rule.getPauseTime() == null || rule.getPauseTime().before(now);
    }

    private void check(AlarmRule rule, NodeAlarmEvent alarmEvent) {
        if (!inPeriod(rule)) {
            return;
        }

        String matchValue = rule.getMatchValue();
        matchValue = StringUtils.substringBeforeLast(matchValue, "@");

        String[] matchValues = StringUtils.split(matchValue, ",");

        for (String match : matchValues) {
            if (StringUtils.containsIgnoreCase(alarmEvent.getMessage(), match)) {
                String message = String.format(MESAGE_FORMAT, alarmEvent.getPipelineId(), alarmEvent.getNid(),
                                               alarmEvent.getMessage());
                sendAlarm(rule, message);
                break;
            }
        }
    }
}
