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

package com.alibaba.otter.manager.biz.statistics.throughput.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.statistics.throughput.ThroughputStatService;
import com.alibaba.otter.manager.biz.statistics.throughput.dal.ThroughputDAO;
import com.alibaba.otter.manager.biz.statistics.throughput.dal.dataobject.ThroughputStatDO;
import com.alibaba.otter.manager.biz.statistics.throughput.param.AnalysisType;
import com.alibaba.otter.manager.biz.statistics.throughput.param.RealtimeThroughputCondition;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputCondition;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputInfo;
import com.alibaba.otter.manager.biz.statistics.throughput.param.TimelineThroughputCondition;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;

/**
 * @author danping.yudp
 */
public class ThroughputStatServiceImpl implements ThroughputStatService {

    private ThroughputDAO throughputDao;

    public ThroughputDAO getThroughputDao() {
        return throughputDao;
    }

    public void setThroughputDao(ThroughputDAO throughputDao) {
        this.throughputDao = throughputDao;
    }

    /**
     * 在数据库中插入throughputStat
     */
    public void createOrUpdateThroughput(ThroughputStat item) {
        Assert.assertNotNull(item);
        throughputDao.insertThroughputStat(throughputStatModelToDo(item));
    }

    public ThroughputStat findThroughputStatByPipelineId(ThroughputCondition condition) {
        Assert.assertNotNull(condition);
        ThroughputStatDO throughputStatDO = throughputDao.findRealtimeThroughputStat(condition);
        ThroughputStat throughputStat = new ThroughputStat();
        if (throughputStatDO != null) {
            throughputStat = throughputStatDOToModel(throughputStatDO);
        }
        return throughputStat;
    }

    /**
     * 3种时间间隔的统计信息
     */

    public Map<AnalysisType, ThroughputInfo> listRealtimeThroughput(RealtimeThroughputCondition condition) {
        Assert.assertNotNull(condition);
        Map<AnalysisType, ThroughputInfo> throughputInfos = new HashMap<AnalysisType, ThroughputInfo>();
        TimelineThroughputCondition timelineCondition = new TimelineThroughputCondition();
        Date realtime = new Date(System.currentTimeMillis());
        timelineCondition.setPipelineId(condition.getPipelineId());
        timelineCondition.setType(condition.getType());
        timelineCondition.setStart(new Date(realtime.getTime() - condition.getMax() * 60 * 1000));
        timelineCondition.setEnd(realtime);
        List<ThroughputStatDO> throughputStatDOs = throughputDao.listTimelineThroughputStat(timelineCondition);
        for (AnalysisType analysisType : condition.getAnalysisType()) {
            ThroughputInfo throughputInfo = new ThroughputInfo();
            List<ThroughputStat> throughputStat = new ArrayList<ThroughputStat>();
            for (ThroughputStatDO throughputStatDO : throughputStatDOs) {
                if (realtime.getTime() - throughputStatDO.getEndTime().getTime() <= analysisType.getValue() * 60 * 1000) {
                    throughputStat.add(throughputStatDOToModel(throughputStatDO));
                }
            }
            throughputInfo.setItems(throughputStat);
            throughputInfo.setSeconds(analysisType.getValue() * 60L);
            throughputInfos.put(analysisType, throughputInfo);
        }
        return throughputInfos;

    }

    /**
     * <pre>
     * 列出pipeLineId下，start-end时间段下的throughputStat 
     * 首先从数据库中取出这一段时间所以数据，该数据都是根据end_time倒排序的, 每隔1分钟将这些数据分组
     * </pre>
     */
    public Map<Long, ThroughputInfo> listTimelineThroughput(TimelineThroughputCondition condition) {
        Assert.assertNotNull(condition);
        Map<Long, ThroughputInfo> throughputInfos = new LinkedHashMap<Long, ThroughputInfo>();
        List<ThroughputStatDO> throughputStatDOs = throughputDao.listTimelineThroughputStat(condition);
        int size = throughputStatDOs.size();
        int k = size - 1;
        for (Long i = condition.getStart().getTime(); i <= condition.getEnd().getTime(); i += 60 * 1000) {
            ThroughputInfo throughputInfo = new ThroughputInfo();
            List<ThroughputStat> throughputStat = new ArrayList<ThroughputStat>();
            // 取出每个时间点i以内的数据，k是一个游标，每次遍历时前面已经取过了的数据就不用再遍历了
            for (int j = k; j >= 0; --j) {
                if ((i - throughputStatDOs.get(j).getEndTime().getTime() <= 60 * 1000)
                    && (i - throughputStatDOs.get(j).getEndTime().getTime() >= 0)) {
                    throughputStat.add(throughputStatDOToModel(throughputStatDOs.get(j)));
                    k = j - 1;
                }// 如果不满足if条件，则后面的数据也不用再遍历
                else {
                    break;
                }
            }
            if (throughputStat.size() > 0) {
                throughputInfo.setItems(throughputStat);
                throughputInfo.setSeconds(1 * 60L);
                throughputInfos.put(i, throughputInfo);
            }

        }
        return throughputInfos;
    }

    public List<ThroughputStat> listRealtimeThroughputByPipelineIds(List<Long> pipelineIds, int minute) {
        Assert.assertNotNull(pipelineIds);
        List<ThroughputStatDO> throughputStatDOs = throughputDao.listRealTimeThroughputStatByPipelineIds(pipelineIds,
                                                                                                         minute);

        List<ThroughputStat> infos = new ArrayList<ThroughputStat>();
        for (ThroughputStatDO throughputStatDO : throughputStatDOs) {
            infos.add(throughputStatDOToModel(throughputStatDO));
        }

        return infos;
    }

    /**
     * 用于Model对象转化为DO对象
     * 
     * @param throughputStat
     * @return throughputStatDO
     */
    private ThroughputStatDO throughputStatModelToDo(ThroughputStat throughputStat) {
        ThroughputStatDO throughputStatDO = new ThroughputStatDO();
        throughputStatDO.setId(throughputStat.getId());
        throughputStatDO.setPipelineId(throughputStat.getPipelineId());
        throughputStatDO.setStartTime(throughputStat.getStartTime());
        throughputStatDO.setEndTime(throughputStat.getEndTime());
        throughputStatDO.setType(throughputStat.getType());
        throughputStatDO.setNumber(throughputStat.getNumber());
        throughputStatDO.setSize(throughputStat.getSize());
        throughputStatDO.setGmtCreate(throughputStat.getGmtCreate());
        throughputStatDO.setGmtModified(throughputStat.getGmtModified());
        return throughputStatDO;
    }

    /**
     * 用于DO对象转化为Model对象
     * 
     * @param throughputStatDO
     * @return throughputStat
     */
    private ThroughputStat throughputStatDOToModel(ThroughputStatDO throughputStatDO) {
        ThroughputStat throughputStat = new ThroughputStat();
        throughputStat.setId(throughputStatDO.getId());
        throughputStat.setPipelineId(throughputStatDO.getPipelineId());
        throughputStat.setStartTime(throughputStatDO.getStartTime());
        throughputStat.setEndTime(throughputStatDO.getEndTime());
        throughputStat.setType(throughputStatDO.getType());
        throughputStat.setNumber(throughputStatDO.getNumber());
        throughputStat.setSize(throughputStatDO.getSize());
        throughputStat.setGmtCreate(throughputStatDO.getGmtCreate());
        throughputStat.setGmtModified(throughputStatDO.getGmtModified());
        return throughputStat;
    }

}
