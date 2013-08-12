package com.alibaba.otter.shared.common.model.config.alarm;

/**
 * @author simon 2012-8-29 下午7:34:31
 * @version 4.1.0
 */

public enum AlarmRuleStatus {
    ENABLE, PAUSED, DISABLE;

    public boolean isEnable() {
        return this.equals(AlarmRuleStatus.ENABLE);
    }

    public boolean isPaused() {
        return this.equals(AlarmRuleStatus.PAUSED);
    }

    public boolean isDisable() {
        return this.equals(AlarmRuleStatus.DISABLE);
    }
}
