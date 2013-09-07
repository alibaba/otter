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

package com.alibaba.otter.manager.web.home.module.action;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import com.alibaba.citrus.service.form.CustomErrors;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.alarm.AlarmRuleService;
import com.alibaba.otter.manager.biz.config.parameter.SystemParameterService;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRuleStatus;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;

public class AlarmRuleAction extends AbstractAction {

    @Resource(name = "alarmRuleService")
    private AlarmRuleService       alarmRuleService;

    @Resource(name = "systemParameterService")
    private SystemParameterService systemParameterService;

    public void doAdd(@FormGroup("alarmRuleInfo") Group alarmRuleInfo,
                      @FormField(name = "formAlarmRuleError", group = "alarmRuleInfo") CustomErrors err, Navigator nav)
                                                                                                                       throws Exception {
        AlarmRule alarmRule = new AlarmRule();
        alarmRuleInfo.setProperties(alarmRule);

        try {
            alarmRuleService.create(alarmRule);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidAlarmRule");
            return;
        }
        nav.redirectToLocation("alarmRuleList.htm?pipelineId=" + alarmRule.getPipelineId());
    }

    /**
     * 一键添加监控
     */
    public void doOnekeyAddMonitor(@Param("pipelineId") Long pipelineId, Navigator nav) throws Exception {
        List<AlarmRule> existRules = alarmRuleService.getAlarmRules(pipelineId);
        if (!existRules.isEmpty()) {
            nav.redirectToLocation("alarmRuleList.htm?pipelineId=" + pipelineId);
            return;
        }

        SystemParameter systemParameter = systemParameterService.find();
        AlarmRule alarmRule = new AlarmRule();
        alarmRule.setPipelineId(pipelineId);
        alarmRule.setDescription("one key added!");
        alarmRule.setAutoRecovery(Boolean.FALSE);
        alarmRule.setReceiverKey(systemParameter.getDefaultAlarmReceiveKey());
        alarmRule.setStatus(AlarmRuleStatus.DISABLE);
        alarmRule.setRecoveryThresold(3);
        alarmRule.setIntervalTime(1800L);

        try {
            alarmRule.setMonitorName(MonitorName.EXCEPTION);
            alarmRule.setMatchValue("ERROR,EXCEPTION");
            alarmRule.setIntervalTime(1800L);
            alarmRule.setAutoRecovery(false);
            alarmRule.setRecoveryThresold(2);
            alarmRuleService.create(alarmRule);
            alarmRule.setMonitorName(MonitorName.POSITIONTIMEOUT);
            alarmRule.setMatchValue("600");
            alarmRule.setIntervalTime(1800L);
            alarmRule.setAutoRecovery(true);
            alarmRule.setRecoveryThresold(0);
            alarmRuleService.create(alarmRule);
            alarmRule.setMonitorName(MonitorName.DELAYTIME);
            alarmRule.setMatchValue("600");
            alarmRule.setIntervalTime(1800L);
            alarmRule.setAutoRecovery(false);
            alarmRule.setRecoveryThresold(2);
            alarmRuleService.create(alarmRule);
            alarmRule.setMonitorName(MonitorName.PROCESSTIMEOUT);
            alarmRule.setMatchValue("60");
            alarmRule.setIntervalTime(1800L);
            alarmRule.setAutoRecovery(true);
            alarmRule.setRecoveryThresold(2);
            alarmRuleService.create(alarmRule);
            // alarmRule.setMonitorName(MonitorName.PIPELINETIMEOUT);
            // alarmRule.setMatchValue("43200");
            // alarmRuleService.create(alarmRule);
        } catch (Exception e) {
            return;
        }
        nav.redirectToLocation("alarmRuleList.htm?pipelineId=" + pipelineId);
    }

    /**
     * 修改Node
     */
    public void doEdit(@FormGroup("alarmRuleInfo") Group alarmRuleInfo,
                       @FormField(name = "formAlarmRuleError", group = "alarmRuleInfo") CustomErrors err, Navigator nav)
                                                                                                                        throws Exception {
        AlarmRule alarmRule = new AlarmRule();
        alarmRuleInfo.setProperties(alarmRule);

        try {
            alarmRuleService.modify(alarmRule);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidAlarmRule");
            return;
        }
        nav.redirectToLocation("alarmRuleList.htm?pipelineId=" + alarmRule.getPipelineId());
    }

    /**
     * 删除node
     */
    public void doDelete(@Param("alarmRuleId") Long alarmRuleId, @Param("pipelineId") Long pipelineId, Navigator nav)
                                                                                                                     throws WebxException {
        alarmRuleService.remove(alarmRuleId);

        nav.redirectToLocation("alarmRuleList.htm?pipelineId=" + pipelineId);

    }

    public void doStatus(@Param("alarmRuleId") Long alarmRuleId, @Param("pipelineId") Long pipelineId,
                         @Param("status") String status, @Param("pauseTime") String pauseTime, Navigator nav)
                                                                                                             throws WebxException {
        if (status.equals("enable")) {
            alarmRuleService.enableMonitor(alarmRuleId);
        } else if (status.equals("disable")) {
            if (pauseTime != null) {
                alarmRuleService.disableMonitor(alarmRuleId, pauseTime);
            } else {
                alarmRuleService.disableMonitor(alarmRuleId);
            }
        }

        nav.redirectToLocation("alarmRuleList.htm?pipelineId=" + pipelineId);
    }

    public void doStatusSystem(@Param("alarmRuleId") Long alarmRuleId, @Param("pageIndex") int pageIndex,
                               @Param("status") String status, @Param("pauseTime") String pauseTime, Navigator nav)
                                                                                                                   throws WebxException {

        if (status.equals("enable")) {
            alarmRuleService.enableMonitor(alarmRuleId);
        } else if (status.equals("disable")) {
            if (pauseTime != null) {
                alarmRuleService.disableMonitor(alarmRuleId, pauseTime);
            } else {
                alarmRuleService.disableMonitor(alarmRuleId);
            }
        }
        nav.redirectToLocation("alarmSystemList.htm?pageIndex=" + pageIndex);
    }

    public void doStatusByPipeline(@Param("pipelineId") Long pipelineId, @Param("status") String status,
                                   @Param("pauseTime") String pauseTime, Navigator nav) throws WebxException {

        List<AlarmRule> alarmRules = alarmRuleService.getAlarmRules(pipelineId);
        for (AlarmRule alarmRule : alarmRules) {
            if (status.equals("enable")) {
                if (alarmRule.getStatus().isDisable()) {
                    alarmRuleService.enableMonitor(alarmRule.getId());
                }
            } else if (status.equals("disable")) {
                if (alarmRule.getStatus().isEnable()) {
                    if (pauseTime != null) {
                        alarmRuleService.disableMonitor(alarmRule.getId(), pauseTime);
                    } else {
                        alarmRuleService.disableMonitor(alarmRule.getId());
                    }
                }
            }
        }
        nav.redirectToLocation("alarmRuleList.htm?pipelineId=" + pipelineId);
    }

    public void doStatusByRule(@Param("alarmRuleIds") String alarmRuleIds, @Param("pageIndex") int pageIndex,
                               @Param("status") String status, @Param("pauseTime") String pauseTime, Navigator nav)
                                                                                                                   throws WebxException {

        String[] a = alarmRuleIds.split(",");
        List<String> alarmRuleArray = Arrays.asList(a);
        for (String alarmRuleId : alarmRuleArray) {
            if (status.equals("enable")) {
                alarmRuleService.enableMonitor(Long.valueOf(alarmRuleId));
            } else if (status.equals("disable")) {
                if (pauseTime != null) {
                    alarmRuleService.disableMonitor(Long.valueOf(alarmRuleId), pauseTime);
                } else {
                    alarmRuleService.disableMonitor(Long.valueOf(alarmRuleId));
                }
            }
        }
        nav.redirectToLocation("alarmSystemList.htm?pageIndex=" + pageIndex);
    }
}
