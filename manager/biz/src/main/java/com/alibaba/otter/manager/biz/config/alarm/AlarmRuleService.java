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

package com.alibaba.otter.manager.biz.config.alarm;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRuleStatus;

/**
 * @author simon 2012-8-24 上午5:27:35
 * @author zebin.xuzb
 * @version 4.1.0
 */
public interface AlarmRuleService {

    void create(AlarmRule alarmRule);

    void modify(AlarmRule alarmRule);

    void remove(Long alarmRuleId);

    public void enableMonitor(Long alarmRuleId);

    public void disableMonitor(Long alarmRuleId);

    public void disableMonitor(Long alarmRuleId, String pauseTime);

    List<AlarmRule> getAllAlarmRules(AlarmRuleStatus status);

    AlarmRule getAlarmRuleById(Long AlarmRuleId);

    /**
     * 获取所有状态为 status 的 {@linkplain AlarmRule}，并且按照pipelineId分区
     * 
     * @param status
     * @return
     */
    Map<Long, List<AlarmRule>> getAlarmRules(AlarmRuleStatus status);

    List<AlarmRule> getAlarmRules(Long pipelineId);

    List<AlarmRule> getAlarmRules(Long pipelineId, AlarmRuleStatus status);

    List<AlarmRule> listAllAlarmRules(Map condition);

    public int getCount();

}
