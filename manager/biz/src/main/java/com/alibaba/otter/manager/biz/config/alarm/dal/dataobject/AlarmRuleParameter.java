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

/**
 * @author simon 2012-9-12 上午10:35:13
 * @version 4.1.0
 */
public class AlarmRuleParameter implements Serializable {

    private static final long serialVersionUID = 1570395344191530689L;
    private Long              intervalTime     = 1800L;
    private String            pauseTime;
    private Integer           recoveryThresold = 3;
    private Boolean           autoRecovery     = false;

    public Long getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(Long intervalTime) {
        this.intervalTime = intervalTime;
    }

    public String getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(String pauseTime) {
        this.pauseTime = pauseTime;
    }

    public Boolean getAutoRecovery() {
        return autoRecovery;
    }

    public void setAutoRecovery(Boolean autoRecovery) {
        this.autoRecovery = autoRecovery;
    }

    public Integer getRecoveryThresold() {
        return recoveryThresold;
    }

    public void setRecoveryThresold(Integer recoveryThresold) {
        this.recoveryThresold = recoveryThresold;
    }

}
