package com.alibaba.otter.manager.biz.statistics.throughput.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.statistics.throughput.dal.dataobject.ThroughputStatDO;
import com.alibaba.otter.manager.biz.statistics.throughput.param.RealtimeThroughputCondition;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputCondition;
import com.alibaba.otter.manager.biz.statistics.throughput.param.TimelineThroughputCondition;

/**
 * @author simon
 */
public interface ThroughputDAO {

    public void insertThroughputStat(ThroughputStatDO throughputStat);

    public void deleteThroughputStat(Long throughputStatId);

    public void modifyThroughputStat(ThroughputStatDO throughputStat);

    public ThroughputStatDO findThroughputStatById(Long throughputStatId);

    public List<ThroughputStatDO> listRealtimeThroughputStat(RealtimeThroughputCondition condition);

    public List<ThroughputStatDO> listTimelineThroughputStat(TimelineThroughputCondition condition);

    public ThroughputStatDO findRealtimeThroughputStat(ThroughputCondition condition);

    public List<ThroughputStatDO> listThroughputStatByPipelineId(Long pipelineId);

    public List<ThroughputStatDO> listRealTimeThroughputStatByPipelineIds(List<Long> pipelineIds, int minute);
}
