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
