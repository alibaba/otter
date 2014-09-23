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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.manager.biz.monitor.MonitorRuleExplorerRegisty;
import com.alibaba.otter.manager.biz.statistics.stage.ProcessStatService;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.alibaba.otter.shared.common.model.statistics.stage.ProcessStat;

/**
 * @author zebin.xuzb @ 2012-8-29
 * @version 4.1.0
 */
public class ProcessTimeoutRuleMonitor extends AbstractRuleMonitor {

    private static final String TIME_OUT_MESSAGE = "pid:%s processIds:%s elapsed %s seconds";

    @Resource(name = "processStatService")
    private ProcessStatService  processStatService;

    ProcessTimeoutRuleMonitor(){
        MonitorRuleExplorerRegisty.register(MonitorName.PIPELINETIMEOUT, this);
    }

    @Override
    public void explore(List<AlarmRule> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }
        Long pipelineId = rules.get(0).getPipelineId();

        List<ProcessStat> processStats = processStatService.listRealtimeProcessStat(pipelineId);
        if (CollectionUtils.isEmpty(processStats)) {
            return;
        }

        long now = System.currentTimeMillis();
        Map<Long, Long> processTime = new HashMap<Long, Long>();
        for (ProcessStat processStat : processStats) {
            Long timeout = 0L;
            if (!CollectionUtils.isEmpty(processStat.getStageStats())) {
                timeout = now - processStat.getStageStats().get(0).getStartTime();
            }
            processTime.put(processStat.getProcessId(), timeout);
        }

        String message = StringUtils.EMPTY;
        for (AlarmRule rule : rules) {
            if (message.isEmpty()) {
                message = checkTimeout(rule, processTime);
            } else {
                checkTimeout(rule, processTime);
            }
        }

        if (!message.isEmpty()) {
            logRecordAlarm(pipelineId, MonitorName.PROCESSTIMEOUT, message);
        }

    }

    private String checkTimeout(AlarmRule rule, Map<Long, Long> processTime) {
        if (!inPeriod(rule)) {
            return StringUtils.EMPTY;
        }

        String matchValue = rule.getMatchValue();
        matchValue = StringUtils.substringBeforeLast(matchValue, "@");
        Long maxSpentTime = Long.parseLong(StringUtils.trim(matchValue));
        List<Long> timeoutProcessIds = new LinkedList<Long>();
        Collections.sort(timeoutProcessIds);
        long maxSpent = 0;
        for (Entry<Long, Long> entry : processTime.entrySet()) {
            // maxSpentTime 是秒，而processTime的value是毫秒
            if (entry.getValue() >= (maxSpentTime * 1000)) {
                timeoutProcessIds.add(entry.getKey());
                maxSpent = maxSpent > entry.getValue() ? maxSpent : entry.getValue();
            }
        }

        if (CollectionUtils.isEmpty(timeoutProcessIds)) {
            return StringUtils.EMPTY;
        }

        String processIds = StringUtils.join(timeoutProcessIds, ",");
        String message = String.format(TIME_OUT_MESSAGE, rule.getPipelineId(), processIds, (maxSpent / 1000));
        sendAlarm(rule, message);
        return message;
    }
}
