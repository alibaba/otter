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

package com.alibaba.otter.manager.biz.statistics.delay.dal.ibatis;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.statistics.delay.dal.DelayStatDAO;
import com.alibaba.otter.manager.biz.statistics.delay.dal.dataobject.DelayStatDO;
import com.alibaba.otter.manager.biz.statistics.delay.param.TimelineDelayCondition;
import com.alibaba.otter.manager.biz.statistics.delay.param.TopDelayStat;

/**
 * @author danping.yudp
 */
public class IbatisDelayStatDAO extends SqlMapClientDaoSupport implements DelayStatDAO {

    @Override
    public void insertDelayStat(DelayStatDO delayStat) {
        Assert.assertNotNull(delayStat);
        getSqlMapClientTemplate().insert("insertDelayStat", delayStat);
    }

    @Override
    public void deleteDelayStat(Long delayStatId) {
        Assert.assertNotNull(delayStatId);
        getSqlMapClientTemplate().delete("deleteDelayStatById", delayStatId);
    }

    @Override
    public void modifyDelayStat(DelayStatDO delayStat) {
        Assert.assertNotNull(delayStat);
        getSqlMapClientTemplate().update("modifyDelayStat", delayStat);
    }

    @Override
    public DelayStatDO findDelayStatById(Long delayStatId) {
        Assert.assertNotNull(delayStatId);
        return (DelayStatDO) getSqlMapClientTemplate().queryForObject("findDelayStatById", delayStatId);
    }

    @Override
    public DelayStatDO findRealtimeDelayStat(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        return (DelayStatDO) getSqlMapClientTemplate().queryForObject("findRealtimeDelayStat", pipelineId);
    }

    @Override
    public List<DelayStatDO> listDelayStatsByPipelineId(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        return (List<DelayStatDO>) getSqlMapClientTemplate().queryForList("listDelayStatsByPipelineId", pipelineId);
    }

    @Override
    public List<DelayStatDO> listTimelineDelayStatsByPipelineId(Long pipelineId, Date start, Date end) {
        TimelineDelayCondition tdc = new TimelineDelayCondition();
        tdc.setPipelineId(pipelineId);
        tdc.setStart(start);
        tdc.setEnd(end);
        Assert.assertNotNull(tdc);
        return (List<DelayStatDO>) getSqlMapClientTemplate().queryForList("listTimelineDelayStatsByPipelineId", tdc);
    }

    public List<TopDelayStat> listTopDelayStatsByName(String searchKey, int topN) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("searchKey", searchKey);
        param.put("topN", topN);
        return (List<TopDelayStat>) getSqlMapClientTemplate().queryForList("listTopByName", param);
    }

}
