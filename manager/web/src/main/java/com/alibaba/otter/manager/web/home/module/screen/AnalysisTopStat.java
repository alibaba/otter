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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.statistics.delay.DelayStatService;
import com.alibaba.otter.manager.biz.statistics.delay.param.TopDelayStat;
import com.alibaba.otter.manager.biz.statistics.delay.param.TopDelayStat.DataStat;
import com.alibaba.otter.manager.biz.statistics.throughput.ThroughputStatService;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.arbitrate.ArbitrateViewService;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputType;

public class AnalysisTopStat {

    @Resource(name = "throughputStatService")
    private ThroughputStatService  throughputStatService;

    @Resource(name = "delayStatService")
    private DelayStatService       delayStatService;

    @Resource(name = "arbitrateManageService")
    private ArbitrateManageService arbitrateManageService;

    @Resource(name = "arbitrateViewService")
    private ArbitrateViewService   arbitrateViewService;

    public void execute(@Param("searchKey") String searchKey, @Param("topN") int topN, @Param("statTime") int minute,
                        Context context) throws Exception {

        if (topN <= 0) {
            topN = 15;
        }

        if (minute <= 0) {
            minute = 1;
        }

        List<TopDelayStat> tops = delayStatService.listTopDelayStat(searchKey, topN);

        List<Long> pipelineIds = new ArrayList<Long>();
        for (TopDelayStat top : tops) {
            top.setStatTime(Long.valueOf(minute));
            pipelineIds.add(top.getPipelineId());
        }

        Map<Long, ChannelStatus> channelStatuses = new HashMap<Long, ChannelStatus>(tops.size(), 1f);
        Map<Long, MainStemEventData> mainstemStatuses = new HashMap<Long, MainStemEventData>(tops.size(), 1f);

        if (pipelineIds.size() > 0) {
            List<ThroughputStat> stats = throughputStatService.listRealtimeThroughputByPipelineIds(pipelineIds, minute);
            for (ThroughputStat stat : stats) {
                for (TopDelayStat top : tops) {
                    if (stat.getPipelineId().equals(top.getPipelineId())) {
                        DataStat s = new DataStat(stat.getNumber(), stat.getSize());
                        if (ThroughputType.FILE == stat.getType()) {
                            top.setFileStat(s);
                        } else if (ThroughputType.ROW == stat.getType()) {
                            top.setDbStat(s);
                        }
                        break;
                    }
                }
            }

            for (TopDelayStat top : tops) {
                if (!channelStatuses.containsKey(top.getChannelId())) {
                    channelStatuses.put(top.getChannelId(),
                                        arbitrateManageService.channelEvent().status(top.getChannelId()));
                }

                if (!mainstemStatuses.containsKey(top.getPipelineId())) {
                    mainstemStatuses.put(top.getPipelineId(),
                                         arbitrateViewService.mainstemData(top.getChannelId(), top.getPipelineId()));
                }
            }
        }

        context.put("tops", tops);
        context.put("statTime", minute);
        context.put("channelStatuses", channelStatuses);
        context.put("mainstemStatuses", mainstemStatuses);
    }
}
