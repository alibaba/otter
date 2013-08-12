package com.alibaba.otter.manager.biz.statistics.throughput;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.manager.biz.statistics.throughput.param.AnalysisType;
import com.alibaba.otter.manager.biz.statistics.throughput.param.RealtimeThroughputCondition;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputCondition;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputInfo;
import com.alibaba.otter.manager.biz.statistics.throughput.param.TimelineThroughputCondition;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;

/**
 * @author jianghang 2011-9-8 下午01:26:31
 */
public interface ThroughputStatService {

    public Map<AnalysisType, ThroughputInfo> listRealtimeThroughput(RealtimeThroughputCondition condition);

    public Map<Long, ThroughputInfo> listTimelineThroughput(TimelineThroughputCondition condition);

    public List<ThroughputStat> listRealtimeThroughputByPipelineIds(List<Long> pipelineIds, int minute);

    public ThroughputStat findThroughputStatByPipelineId(ThroughputCondition condition);

    public void createOrUpdateThroughput(ThroughputStat item);
}
