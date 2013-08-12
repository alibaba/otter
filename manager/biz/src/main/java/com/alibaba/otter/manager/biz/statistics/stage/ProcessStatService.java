package com.alibaba.otter.manager.biz.statistics.stage;

import java.util.Date;
import java.util.List;

import com.alibaba.otter.shared.common.model.statistics.stage.ProcessStat;

/**
 * @author jianghang 2011-9-8 下午12:48:21
 */
public interface ProcessStatService {

    public List<ProcessStat> listRealtimeProcessStat(Long pipelineId);

    public List<ProcessStat> listRealtimeProcessStat(Long channelId, Long pipelineId);

    public List<ProcessStat> listTimelineProcessStat(Long pipelineId, Date start, Date end);

    public void createProcessStat(ProcessStat stat);
}
