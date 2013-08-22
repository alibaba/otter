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

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.manager.biz.monitor.MonitorRuleExplorerRegisty;
import com.alibaba.otter.manager.biz.statistics.throughput.ThroughputStatService;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputCondition;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputType;

/**
 * @author zebin.xuzb @ 2012-8-29
 * @version 4.1.0
 */
public class PipelineTimeoutRuleMonitor extends AbstractRuleMonitor {

    private static final String   TIME_OUT_MESSAGE = "pid:%s elapsed %s seconds no sync";

    @Resource(name = "throughputStatService")
    private ThroughputStatService throughputStatService;

    PipelineTimeoutRuleMonitor(){
        MonitorRuleExplorerRegisty.register(MonitorName.PIPELINETIMEOUT, this);
    }

    @Override
    public void explore(List<AlarmRule> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }
        Long pipelineId = rules.get(0).getPipelineId();

        ThroughputCondition condition = new ThroughputCondition();
        condition.setPipelineId(pipelineId);
        condition.setType(ThroughputType.ROW);
        ThroughputStat stat = throughputStatService.findThroughputStatByPipelineId(condition);

        long latestSyncTime = 0L;
        if (stat != null && stat.getGmtModified() != null) {
            Date modifiedDate = stat.getGmtModified();
            latestSyncTime = modifiedDate.getTime();
        }
        long now = System.currentTimeMillis();
        long elapsed = now - latestSyncTime;
        boolean flag = false;
        for (AlarmRule rule : rules) {
            flag |= checkTimeout(rule, elapsed);
        }

        if (flag) {
            logRecordAlarm(pipelineId, MonitorName.PIPELINETIMEOUT,
                           String.format(TIME_OUT_MESSAGE, pipelineId, (elapsed / 1000)));
        }
    }

    private boolean checkTimeout(AlarmRule rule, long elapsed) {
        if (!inPeriod(rule)) {
            return false;
        }

        String matchValue = rule.getMatchValue();
        matchValue = StringUtils.substringBeforeLast(matchValue, "@");
        Long maxSpentTime = Long.parseLong(StringUtils.trim(matchValue));
        // sinceLastSync是毫秒，而 maxSpentTime 是秒
        if (elapsed >= (maxSpentTime * 1000)) {
            sendAlarm(rule, String.format(TIME_OUT_MESSAGE, rule.getPipelineId(), (elapsed / 1000)));
            return true;
        }
        return false;
    }
}
