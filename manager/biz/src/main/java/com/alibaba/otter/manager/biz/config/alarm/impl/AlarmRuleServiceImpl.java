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

package com.alibaba.otter.manager.biz.config.alarm.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.config.alarm.AlarmRuleService;
import com.alibaba.otter.manager.biz.config.alarm.dal.AlarmRuleDAO;
import com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO;
import com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleParameter;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRuleStatus;
import com.alibaba.otter.shared.common.utils.Assert;

/**
 * @author simon 2012-8-24 上午5:29:55
 * @version 4.1.0
 */
public class AlarmRuleServiceImpl implements AlarmRuleService {

    private static final Logger logger           = LoggerFactory.getLogger(AlarmRuleServiceImpl.class);
    public static final String  TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private AlarmRuleDAO        alarmRuleDao;

    public void create(AlarmRule alarmRule) {
        Assert.assertNotNull(alarmRule);
        alarmRuleDao.insert(modelToDo(alarmRule));
    }

    public void modify(AlarmRule alarmRule) {
        AlarmRuleDO alarmRuleDo = modelToDo(alarmRule);
        alarmRuleDao.update(alarmRuleDo);

    }

    public void remove(Long alarmRuleId) {
        alarmRuleDao.delete(alarmRuleId);
    }

    private void switchAlarmRuleStatus(Long alarmRuleId, AlarmRuleStatus alarmRuleStatus, String pauseTime) {
        AlarmRuleDO alarmRuleDo = alarmRuleDao.findById(alarmRuleId);

        if (null == alarmRuleDo) {
            String exceptionCause = "query alarmRule:" + alarmRuleId + " return null.";
            logger.error("ERROR ## " + exceptionCause);
            throw new ManagerException(exceptionCause);
        }

        alarmRuleDo.setStatus(alarmRuleStatus);
        if (alarmRuleDo.getAlarmRuleParameter() != null) {
            alarmRuleDo.getAlarmRuleParameter().setPauseTime(pauseTime);
        } else if (StringUtils.isNotEmpty(pauseTime)) {
            alarmRuleDo.setAlarmRuleParameter(new AlarmRuleParameter());
            alarmRuleDo.getAlarmRuleParameter().setPauseTime(pauseTime);
        }
        alarmRuleDao.update(alarmRuleDo);
    }

    public void enableMonitor(Long alarmRuleId) {
        switchAlarmRuleStatus(alarmRuleId, AlarmRuleStatus.ENABLE, null);
    }

    public void disableMonitor(Long alarmRuleId) {
        switchAlarmRuleStatus(alarmRuleId, AlarmRuleStatus.DISABLE, null);
    }

    public void disableMonitor(Long alarmRuleId, String pauseTime) {
        switchAlarmRuleStatus(alarmRuleId, AlarmRuleStatus.ENABLE, pauseTime);
    }

    public AlarmRule getAlarmRuleById(Long AlarmRuleId) {
        Assert.assertNotNull(AlarmRuleId);
        return doToModel(alarmRuleDao.findById(AlarmRuleId));
    }

    public List<AlarmRule> getAllAlarmRules(AlarmRuleStatus status) {
        Assert.assertNotNull(status);
        List<AlarmRuleDO> alarmRuleDos = alarmRuleDao.listByStatus(status);
        return doToModel(alarmRuleDos);
    }

    public Map<Long, List<AlarmRule>> getAlarmRules(AlarmRuleStatus status) {
        Assert.assertNotNull(status);
        List<AlarmRule> alarmRules = getAllAlarmRules(status);
        Map<Long, List<AlarmRule>> result = new HashMap<Long, List<AlarmRule>>();
        for (AlarmRule alarmRule : alarmRules) {
            List<AlarmRule> rules = result.get(alarmRule.getPipelineId());
            if (rules == null) {
                rules = new ArrayList<AlarmRule>();
            }
            if (!rules.contains(alarmRule)) {
                rules.add(alarmRule);
            }
            result.put(alarmRule.getPipelineId(), rules);
        }
        return result;
    }

    public List<AlarmRule> getAlarmRules(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        List<AlarmRuleDO> alarmRuleDos = alarmRuleDao.listByPipelineId(pipelineId);
        return doToModel(alarmRuleDos);
    }

    public List<AlarmRule> getAlarmRules(Long pipelineId, AlarmRuleStatus status) {
        Assert.assertNotNull(pipelineId);
        Assert.assertNotNull(status);
        List<AlarmRuleDO> alarmRuleDos = alarmRuleDao.listByPipelineId(pipelineId, status);

        return doToModel(alarmRuleDos);
    }

    public List<AlarmRule> listAllAlarmRules(Map condition) {
        List<AlarmRule> alarmRules = doToModel(alarmRuleDao.listAllByPipeline(condition));
        return alarmRules;
    }

    public int getCount() {
        return alarmRuleDao.getCount();
    }

    private AlarmRule doToModel(AlarmRuleDO alarmRuleDo) {
        AlarmRule alarmRule = new AlarmRule();
        alarmRule.setId(alarmRuleDo.getId());
        alarmRule.setMatchValue(alarmRuleDo.getMatchValue());
        alarmRule.setMonitorName(alarmRuleDo.getMonitorName());
        alarmRule.setReceiverKey(alarmRuleDo.getReceiverKey());
        // 如果数据库里面的数据为空，则返回默认值
        alarmRule.setIntervalTime(alarmRuleDo.getAlarmRuleParameter() == null ? 1800L : alarmRuleDo.getAlarmRuleParameter().getIntervalTime());
        String pauseTime = alarmRuleDo.getAlarmRuleParameter() == null ? null : alarmRuleDo.getAlarmRuleParameter().getPauseTime();
        if (StringUtils.isNotEmpty(pauseTime)) {
            SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP_FORMAT);
            try {
                alarmRule.setPauseTime(format.parse(pauseTime));
            } catch (ParseException e) {
                throw new ManagerException(e);
            }
        }

        alarmRule.setAutoRecovery(alarmRuleDo.getAlarmRuleParameter() == null ? false : alarmRuleDo.getAlarmRuleParameter().getAutoRecovery());
        alarmRule.setRecoveryThresold(alarmRuleDo.getAlarmRuleParameter() == null ? 3 : alarmRuleDo.getAlarmRuleParameter().getRecoveryThresold());
        alarmRule.setPipelineId(alarmRuleDo.getPipelineId());
        alarmRule.setStatus(alarmRuleDo.getStatus());
        alarmRule.setDescription(alarmRuleDo.getDescription());
        alarmRule.setGmtCreate(alarmRuleDo.getGmtCreate());
        alarmRule.setGmtModified(alarmRuleDo.getGmtModified());
        return alarmRule;
    }

    private List<AlarmRule> doToModel(List<AlarmRuleDO> alarmRuleDos) {
        List<AlarmRule> alarmRules = new ArrayList<AlarmRule>();
        for (AlarmRuleDO alarmRuleDo : alarmRuleDos) {
            alarmRules.add(doToModel(alarmRuleDo));
        }
        return alarmRules;
    }

    private AlarmRuleDO modelToDo(AlarmRule alarmRule) {
        AlarmRuleDO alarmRuleDo = new AlarmRuleDO();
        alarmRuleDo.setId(alarmRule.getId());
        alarmRuleDo.setMatchValue(alarmRule.getMatchValue());
        alarmRuleDo.setMonitorName(alarmRule.getMonitorName());
        alarmRuleDo.setReceiverKey(alarmRule.getReceiverKey());
        alarmRuleDo.setPipelineId(alarmRule.getPipelineId());
        alarmRuleDo.setStatus(alarmRule.getStatus());
        alarmRuleDo.setDescription(alarmRule.getDescription());
        alarmRuleDo.setGmtCreate(alarmRule.getGmtCreate());
        alarmRuleDo.setGmtModified(alarmRule.getGmtModified());
        AlarmRuleParameter alarmRuleParameter = new AlarmRuleParameter();
        alarmRuleParameter.setIntervalTime(alarmRule.getIntervalTime());
        if (alarmRule.getPauseTime() != null) {
            SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP_FORMAT);
            alarmRuleParameter.setPauseTime(format.format(alarmRule.getPauseTime()));
        }
        alarmRuleParameter.setAutoRecovery(alarmRule.getAutoRecovery());
        alarmRuleParameter.setRecoveryThresold(alarmRule.getRecoveryThresold());
        alarmRuleDo.setAlarmRuleParameter(alarmRuleParameter);

        return alarmRuleDo;
    }

    public void setAlarmRuleDao(AlarmRuleDAO alarmRuleDao) {
        this.alarmRuleDao = alarmRuleDao;
    }

}
