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

import org.springframework.util.CollectionUtils;

import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.monitor.AlarmRecovery;
import com.alibaba.otter.manager.biz.monitor.MonitorRuleExplorerRegisty;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * 针对挂起异常的监控处理
 * 
 * @author jianghang 2012-9-19 下午05:25:19
 * @version 4.1.0
 */
public class PausedRuleMonitor extends AbstractRuleMonitor {

    private PipelineService        pipelineService;
    private ArbitrateManageService arbitrateManageService;
    private AlarmRecovery          restartAlarmRecovery;

    PausedRuleMonitor(){
        MonitorRuleExplorerRegisty.register(MonitorName.PAUSED, this);
    }

    public void explore(List<AlarmRule> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }

        AlarmRule rule = rules.get(0);
        Pipeline pipeline = pipelineService.findById(rule.getPipelineId());
        ChannelStatus status = arbitrateManageService.channelEvent().status(pipeline.getChannelId());
        if (status.isPause() && rule.getAutoRecovery()) {
            // 出现问题直接恢复
            restartAlarmRecovery.recovery(rule);
        }
    }

    public void setPipelineService(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    public void setArbitrateManageService(ArbitrateManageService arbitrateManageService) {
        this.arbitrateManageService = arbitrateManageService;
    }

    public void setRestartAlarmRecovery(AlarmRecovery restartAlarmRecovery) {
        this.restartAlarmRecovery = restartAlarmRecovery;
    }

}
