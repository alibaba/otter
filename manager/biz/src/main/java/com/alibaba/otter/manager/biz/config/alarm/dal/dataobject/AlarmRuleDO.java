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

package com.alibaba.otter.manager.biz.config.alarm.dal.dataobject;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.otter.shared.common.model.config.alarm.AlarmRuleStatus;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;

/**
 * @author simon 2012-8-22 下午3:42:34
 * @version 4.1.0
 */
public class AlarmRuleDO implements Serializable {

    private static final long  serialVersionUID   = -1284784362325347636L;
    private Long               id;
    private Long               pipelineId;
    private AlarmRuleStatus    status;
    private MonitorName        monitorName;
    private String             receiverKey;
    private String             matchValue;
    private AlarmRuleParameter alarmRuleParameter = new AlarmRuleParameter();
    private String             description;
    private Date               gmtCreate;
    private Date               gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public AlarmRuleStatus getStatus() {
        return status;
    }

    public void setStatus(AlarmRuleStatus status) {
        this.status = status;
    }

    public MonitorName getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(MonitorName monitorName) {
        this.monitorName = monitorName;
    }

    public String getReceiverKey() {
        return receiverKey;
    }

    public void setReceiverKey(String receiverKey) {
        this.receiverKey = receiverKey;
    }

    public String getMatchValue() {
        return matchValue;
    }

    public void setMatchValue(String matchValue) {
        this.matchValue = matchValue;
    }

    public AlarmRuleParameter getAlarmRuleParameter() {
        return alarmRuleParameter;
    }

    public void setAlarmRuleParameter(AlarmRuleParameter alarmRuleParameter) {
        this.alarmRuleParameter = alarmRuleParameter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

}
