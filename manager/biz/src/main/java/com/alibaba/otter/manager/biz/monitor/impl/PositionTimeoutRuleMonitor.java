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

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.monitor.MonitorRuleExplorerRegisty;
import com.alibaba.otter.shared.arbitrate.ArbitrateViewService;
import com.alibaba.otter.shared.arbitrate.model.PositionEventData;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * 位点超时监控
 * 
 * @author jianghang 2012-12-12 上午10:33:12
 * @version 4.1.3
 */
public class PositionTimeoutRuleMonitor extends AbstractRuleMonitor {

    private PipelineService      pipelineService;
    private ArbitrateViewService arbitrateViewService;
    private static final String  TIME_OUT_MESSAGE = "pid:%s position %s seconds no update";

    PositionTimeoutRuleMonitor(){
        MonitorRuleExplorerRegisty.register(MonitorName.POSITIONTIMEOUT, this);
    }

    @Override
    public void explore(List<AlarmRule> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }
        Long pipelineId = rules.get(0).getPipelineId();
        Pipeline pipeline = pipelineService.findById(pipelineId);
        PositionEventData data = arbitrateViewService.getCanalCursor(pipeline.getParameters().getDestinationName(),
                                                                     pipeline.getParameters().getMainstemClientId());

        long latestSyncTime = 0L;
        if (data != null && data.getModifiedTime() != null) {
            Date modifiedDate = data.getModifiedTime();
            latestSyncTime = modifiedDate.getTime();
        } else {
            return;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - latestSyncTime;
        boolean flag = false;
        for (AlarmRule rule : rules) {
            flag |= checkTimeout(rule, elapsed);
        }

        if (flag) {
            logRecordAlarm(pipelineId, MonitorName.POSITIONTIMEOUT,
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

    public void setPipelineService(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    public void setArbitrateViewService(ArbitrateViewService arbitrateViewService) {
        this.arbitrateViewService = arbitrateViewService;
    }

}
