package com.alibaba.otter.manager.biz.monitor;

import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;

/**
 * 报警尝试自行恢复机制
 * 
 * @author jianghang 2012-9-19 下午04:43:30
 * @version 4.1.0
 */
public interface AlarmRecovery {

    /**
     * 根据规则，强制进行recovery操作
     */
    public void recovery(AlarmRule alarmRule);

    /**
     * 根据规则+触发次数，进行recovery操作
     */
    public void recovery(AlarmRule alarmRule, long alarmCount);
}
