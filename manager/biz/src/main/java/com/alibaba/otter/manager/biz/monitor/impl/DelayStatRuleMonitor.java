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

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.manager.biz.statistics.delay.DelayStatService;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.alibaba.otter.shared.common.model.statistics.delay.DelayStat;

/**
 * @author zebin.xuzb @ 2012-8-29
 * @version 4.1.0
 */
public class DelayStatRuleMonitor extends AbstractRuleMonitor {

    private static final String DELAY_NUMBER_MESSAGE = "pid:%s delay_number:%s";
    private static final String DELAY_TIME_MESSAGE   = "pid:%s delay_time:%s seconds";

    @Resource(name = "delayStatService")
    private DelayStatService    delayStatService;

    @Override
    public void explore(List<AlarmRule> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }

        // 进入到监控项级别的rule，pipelineId一定是相同的
        Long pipelineId = rules.get(0).getPipelineId();
        DelayStat delayStat = delayStatService.findRealtimeDelayStat(pipelineId);
        Long delayNumber = 0L;
        Long delayTime = 0L; // seconds
        if (delayStat.getDelayNumber() != null) {
            delayNumber = delayStat.getDelayNumber();

        }
        if (delayStat.getDelayTime() != null) {
            delayTime = delayStat.getDelayTime();
        }

        boolean delayNumberFlag = false;
        boolean delayTimeFlag = false;
        for (AlarmRule rule : rules) {
            if (rule.getMonitorName().isQueueSize()) {
                delayNumberFlag |= checkQueueSize(rule, delayNumber);
            } else if (rule.getMonitorName().isDelayTime()) {
                delayTimeFlag |= checkDelayTime(rule, delayTime);
            }
        }

        if (delayNumberFlag) {
            logRecordAlarm(pipelineId, MonitorName.QUEUESIZE,
                           String.format(DELAY_NUMBER_MESSAGE, pipelineId, delayNumber));
        }

        if (delayTimeFlag) {
            logRecordAlarm(pipelineId, MonitorName.DELAYTIME, String.format(DELAY_TIME_MESSAGE, pipelineId, delayTime));
        }
    }

    private boolean checkQueueSize(AlarmRule rule, Long delayNumber) {

        if (!inPeriod(rule)) {
            return false;
        }

        String matchValue = rule.getMatchValue();
        matchValue = StringUtils.substringBeforeLast(matchValue, "@");
        Long maxDelayNumber = Long.parseLong(StringUtils.trim(matchValue));
        if (delayNumber >= maxDelayNumber) {
            sendAlarm(rule, String.format(DELAY_NUMBER_MESSAGE, rule.getPipelineId(), delayNumber));
            return true;
        }
        return false;
    }

    private boolean checkDelayTime(AlarmRule rule, Long delayTime) {

        if (!inPeriod(rule)) {
            return false;
        }

        String matchValue = rule.getMatchValue();
        matchValue = StringUtils.substringBeforeLast(matchValue, "@");
        Long maxDelayTime = Long.parseLong(StringUtils.trim(matchValue));
        if (delayTime >= maxDelayTime) {
            sendAlarm(rule, String.format(DELAY_TIME_MESSAGE, rule.getPipelineId(), delayTime));
            return true;
        }
        return false;
    }

}
