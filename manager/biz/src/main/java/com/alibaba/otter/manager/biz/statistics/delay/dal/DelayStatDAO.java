package com.alibaba.otter.manager.biz.statistics.delay.dal;

import java.util.Date;
import java.util.List;

import com.alibaba.otter.manager.biz.statistics.delay.dal.dataobject.DelayStatDO;
import com.alibaba.otter.manager.biz.statistics.delay.param.TopDelayStat;

/**
 * @author simon
 */
public interface DelayStatDAO {

    public void insertDelayStat(DelayStatDO delayStat);

    public void deleteDelayStat(Long delayStatId);

    public void modifyDelayStat(DelayStatDO delayStat);

    public DelayStatDO findDelayStatById(Long delayStatId);

    public DelayStatDO findRealtimeDelayStat(Long pipelineId);

    public List<DelayStatDO> listDelayStatsByPipelineId(Long pipelineId);

    public List<DelayStatDO> listTimelineDelayStatsByPipelineId(Long pipelineId, Date start, Date end);

    public List<TopDelayStat> listTopDelayStatsByName(String name, int topN);
}
