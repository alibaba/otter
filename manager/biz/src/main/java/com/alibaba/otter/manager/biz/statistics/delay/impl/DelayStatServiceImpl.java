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

package com.alibaba.otter.manager.biz.statistics.delay.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.statistics.delay.DelayStatService;
import com.alibaba.otter.manager.biz.statistics.delay.dal.DelayStatDAO;
import com.alibaba.otter.manager.biz.statistics.delay.dal.dataobject.DelayStatDO;
import com.alibaba.otter.manager.biz.statistics.delay.param.DelayStatInfo;
import com.alibaba.otter.manager.biz.statistics.delay.param.TopDelayStat;
import com.alibaba.otter.shared.common.model.statistics.delay.DelayStat;

/**
 * @author danping.yudp
 */
public class DelayStatServiceImpl implements DelayStatService {

    private DelayStatDAO delayStatDao;

    public DelayStatDAO getDelayStatDao() {
        return delayStatDao;
    }

    public void setDelayStatDao(DelayStatDAO delayStatDao) {
        this.delayStatDao = delayStatDao;
    }

    /**
     * 在数据库中插入delayStat
     */
    public void createDelayStat(DelayStat stat) {
        Assert.assertNotNull(stat);
        delayStatDao.insertDelayStat(delayStatModelToDo(stat));
    }

    /**
     * 通过pipeLineId得到一个以gmtCreate倒排序的第一条记录
     */
    public DelayStat findRealtimeDelayStat(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        DelayStatDO delayStatDO = delayStatDao.findRealtimeDelayStat(pipelineId);
        DelayStat delayStat = new DelayStat();
        if (delayStatDO != null) {
            delayStat = delayStatDOToModel(delayStatDO);
        }
        return delayStat;
    }

    /**
     * 列出pipeLineId下，start-end时间段下的delayStat
     */
    public Map<Long, DelayStatInfo> listTimelineDelayStat(Long pipelineId, Date start, Date end) {

        Map<Long, DelayStatInfo> delayStatInfos = new LinkedHashMap<Long, DelayStatInfo>();
        List<DelayStatDO> delayStatDOs = delayStatDao.listTimelineDelayStatsByPipelineId(pipelineId, start, end);
        int size = delayStatDOs.size();
        int k = size - 1;
        for (Long i = start.getTime(); i <= end.getTime(); i += 60 * 1000) {
            DelayStatInfo delayStatInfo = new DelayStatInfo();
            List<DelayStat> delayStats = new ArrayList<DelayStat>();
            // 取出每个时间点i以内的数据，k是一个游标，每次遍历时前面已经取过了的数据就不用再遍历了
            for (int j = k; j >= 0; --j) {
                if ((i - delayStatDOs.get(j).getGmtModified().getTime() <= 60 * 1000)
                    && (i - delayStatDOs.get(j).getGmtModified().getTime() >= 0)) {
                    delayStats.add(delayStatDOToModel(delayStatDOs.get(j)));
                    k = j - 1;
                }// 如果不满足if条件，则后面的数据也不用再遍历
                else {
                    break;
                }
            }
            if (delayStats.size() > 0) {
                delayStatInfo.setItems(delayStats);
                delayStatInfos.put(i, delayStatInfo);
            }

        }
        return delayStatInfos;
    }

    public List<TopDelayStat> listTopDelayStat(String searchKey, int topN) {
        return delayStatDao.listTopDelayStatsByName(searchKey, topN);
    }

    /**
     * 用于Model对象转化为DO对象
     * 
     * @param delayStat
     * @return DelayStatDO
     */
    private DelayStatDO delayStatModelToDo(DelayStat delayStat) {
        DelayStatDO delayStatDO = new DelayStatDO();
        delayStatDO.setId(delayStat.getId());
        delayStatDO.setDelayTime(delayStat.getDelayTime());
        delayStatDO.setDelayNumber(delayStat.getDelayNumber());
        delayStatDO.setPipelineId(delayStat.getPipelineId());
        delayStatDO.setGmtCreate(delayStat.getGmtCreate());
        delayStatDO.setGmtModified(delayStat.getGmtModified());
        return delayStatDO;

    }

    /**
     * 用于DO对象转化为Model对象
     * 
     * @param delayStatDO
     * @return DelayStat
     */
    private DelayStat delayStatDOToModel(DelayStatDO delayStatDO) {
        DelayStat delayStat = new DelayStat();
        delayStat.setId(delayStatDO.getId());
        delayStat.setDelayTime(delayStatDO.getDelayTime());
        delayStat.setDelayNumber(delayStatDO.getDelayNumber());
        delayStat.setPipelineId(delayStatDO.getPipelineId());
        delayStat.setGmtCreate(delayStatDO.getGmtCreate());
        delayStat.setGmtModified(delayStatDO.getGmtModified());
        return delayStat;

    }

}
