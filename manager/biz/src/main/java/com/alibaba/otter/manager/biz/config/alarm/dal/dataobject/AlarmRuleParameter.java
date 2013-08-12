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
