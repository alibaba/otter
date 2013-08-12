package com.alibaba.otter.manager.biz.statistics.delay;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.otter.manager.biz.statistics.delay.param.DelayStatInfo;
import com.alibaba.otter.manager.biz.statistics.delay.param.TopDelayStat;
import com.alibaba.otter.shared.common.model.statistics.delay.DelayStat;

/**
 * @author jianghang 2011-9-8 下午12:37:14
 */
public interface DelayStatService {

    public void createDelayStat(DelayStat stat);

    public DelayStat findRealtimeDelayStat(Long pipelineId);

    public Map<Long, DelayStatInfo> listTimelineDelayStat(Long pipelineId, Date start, Date end);

    public List<TopDelayStat> listTopDelayStat(String searchKey, int topN);

}
