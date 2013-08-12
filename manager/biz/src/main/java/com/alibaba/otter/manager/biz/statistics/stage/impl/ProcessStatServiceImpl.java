package com.alibaba.otter.manager.biz.statistics.stage.impl;

import java.util.Date;
import java.util.List;

import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.statistics.stage.ProcessStatService;
import com.alibaba.otter.shared.arbitrate.ArbitrateViewService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.statistics.stage.ProcessStat;

/**
 * @author jianghang 2012-1-10 下午02:10:31
 * @version 4.0.0
 */
public class ProcessStatServiceImpl implements ProcessStatService {

    private ArbitrateViewService arbitrateViewService;
    private ChannelService       channelService;

    public void createProcessStat(ProcessStat stat) {
        throw new UnsupportedOperationException("unsupport method!");
    }

    @Override
    public List<ProcessStat> listRealtimeProcessStat(Long pipelineId) {
        Channel channel = channelService.findByPipelineId(pipelineId);
        return listRealtimeProcessStat(channel.getId(), pipelineId);
    }

    @Override
    public List<ProcessStat> listRealtimeProcessStat(Long channelId, Long pipelineId) {
        return arbitrateViewService.listProcesses(channelId, pipelineId);
    }

    @Override
    public List<ProcessStat> listTimelineProcessStat(Long pipelineId, Date start, Date end) {
        throw new UnsupportedOperationException("unsupport method!");
    }

    // ======================= setter / getter =====================

    public void setArbitrateViewService(ArbitrateViewService arbitrateViewService) {
        this.arbitrateViewService = arbitrateViewService;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

}
