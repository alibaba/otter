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

package com.alibaba.otter.manager.web.home.module.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputType;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.statistics.throughput.ThroughputStatService;
import com.alibaba.otter.manager.biz.statistics.throughput.param.AnalysisType;
import com.alibaba.otter.manager.biz.statistics.throughput.param.RealtimeThroughputCondition;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputCondition;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputInfo;

public class AnalysisThroughputStat {

    @Resource(name = "throughputStatService")
    private ThroughputStatService throughputStatService;

    @Resource(name = "channelService")
    private ChannelService        channelService;

    public void execute(@Param("pipelineId") Long pipelineId, Context context) throws Exception {
        Channel channel = channelService.findByPipelineId(pipelineId);
        RealtimeThroughputCondition condition1 = new RealtimeThroughputCondition();
        RealtimeThroughputCondition condition2 = new RealtimeThroughputCondition();
        ThroughputCondition condition11 = new ThroughputCondition();
        ThroughputCondition condition22 = new ThroughputCondition();
        List<AnalysisType> analysisType = new ArrayList<AnalysisType>();
        analysisType.add(AnalysisType.ONE_MINUTE);
        analysisType.add(AnalysisType.FIVE_MINUTE);
        analysisType.add(AnalysisType.FIFTEEN_MINUTE);
        condition1.setPipelineId(pipelineId);
        condition1.setAnalysisType(analysisType);
        condition1.setType(ThroughputType.FILE);
        condition2.setPipelineId(pipelineId);
        condition2.setAnalysisType(analysisType);
        condition2.setType(ThroughputType.ROW);
        condition11.setPipelineId(pipelineId);
        condition11.setType(ThroughputType.FILE);
        condition22.setPipelineId(pipelineId);
        condition22.setType(ThroughputType.ROW);
        Map<AnalysisType, ThroughputInfo> throughputInfos1 = throughputStatService.listRealtimeThroughput(condition1);
        Map<AnalysisType, ThroughputInfo> throughputInfos2 = throughputStatService.listRealtimeThroughput(condition2);
        ThroughputStat throughputStat1 = throughputStatService.findThroughputStatByPipelineId(condition11);
        ThroughputStat throughputStat2 = throughputStatService.findThroughputStatByPipelineId(condition22);

        context.put("throughputInfos1", throughputInfos1);
        context.put("throughputInfos2", throughputInfos2);
        context.put("channel", channel);
        context.put("pipelineId", pipelineId);
        context.put("throughputStat1", throughputStat1);
        context.put("throughputStat2", throughputStat2);
        context.put("one", AnalysisType.ONE_MINUTE);
        context.put("five", AnalysisType.FIVE_MINUTE);
        context.put("fifteen", AnalysisType.FIFTEEN_MINUTE);
    }
}
