package com.alibaba.otter.manager.biz.config.alarm.dal;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRuleStatus;

/**
 * @author simon 2012-8-24 上午5:17:00
 * @version 4.1.0
 */
public interface AlarmRuleDAO {

    public AlarmRuleDO insert(AlarmRuleDO entityObj);

    public void update(AlarmRuleDO entityObj);

    public void delete(Long id);

    public AlarmRuleDO findById(Long alarmRuleId);

    public List<AlarmRuleDO> listByPipelineId(Long pipelineId);

    public List<AlarmRuleDO> listByPipelineId(Long pipelineId, AlarmRuleStatus status);

    public List<AlarmRuleDO> listAll();

    public List<AlarmRuleDO> listAllByPipeline(Map condition);

    public List<AlarmRuleDO> listByStatus(AlarmRuleStatus status);

    public int getCount();

}
