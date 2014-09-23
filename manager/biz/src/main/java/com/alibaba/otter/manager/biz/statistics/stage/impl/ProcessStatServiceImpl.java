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
