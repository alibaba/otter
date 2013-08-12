package com.alibaba.otter.manager.biz.config.alarm.dal.ibatis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.alarm.dal.AlarmRuleDAO;
import com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRuleStatus;

/**
 * @author simon
 */
public class IbatisAlarmRuleDAO extends SqlMapClientDaoSupport implements AlarmRuleDAO {

    public AlarmRuleDO insert(AlarmRuleDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlMapClientTemplate().insert("insertAlarmRule", entityObj);
        return entityObj;
    }

    public void update(AlarmRuleDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlMapClientTemplate().update("updateAlarmRule", entityObj);
    }

    public void delete(Long id) {
        Assert.assertNotNull(id);
        getSqlMapClientTemplate().update("deleteAlarmRuleById", id);
    }

    public AlarmRuleDO findById(Long alarmRuleId) {
        Assert.assertNotNull(alarmRuleId);
        AlarmRuleDO alarmRuleDo = (AlarmRuleDO) getSqlMapClientTemplate().queryForObject("findByRuleId", alarmRuleId);
        return alarmRuleDo;
    }

    public List<AlarmRuleDO> listByPipelineId(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        List<AlarmRuleDO> alarmRuleDos = getSqlMapClientTemplate().queryForList("listAlarmByPipelineId", pipelineId);
        return alarmRuleDos;
    }

    public List<AlarmRuleDO> listByPipelineId(Long pipelineId, AlarmRuleStatus status) {
        List<AlarmRuleDO> alarmRuleDos = listByPipelineId(pipelineId);
        List<AlarmRuleDO> result = new ArrayList<AlarmRuleDO>();
        for (AlarmRuleDO alarmRuleDo : alarmRuleDos) {
            if (alarmRuleDo.getStatus().equals(status)) {
                result.add(alarmRuleDo);
            }
        }
        return result;
    }

    public List<AlarmRuleDO> listAll() {
        List<AlarmRuleDO> alarmRuleDos = getSqlMapClientTemplate().queryForList("listAllAlarmRule");
        return alarmRuleDos;
    }

    public List<AlarmRuleDO> listAllByPipeline(Map condition) {
        List<AlarmRuleDO> alarmRuleDos = getSqlMapClientTemplate().queryForList("listAllAlarmOrderByPipeline",
                                                                                condition);
        return alarmRuleDos;
    }

    public List<AlarmRuleDO> listByStatus(AlarmRuleStatus status) {
        List<AlarmRuleDO> alarmRuleDos = getSqlMapClientTemplate().queryForList("listAlarmByStatus", status);
        return alarmRuleDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getAlarmRuleCount");
        return count.intValue();
    }

}
