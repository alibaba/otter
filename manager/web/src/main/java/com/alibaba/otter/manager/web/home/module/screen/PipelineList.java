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
import javax.servlet.http.HttpSession;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.alarm.AlarmRuleService;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.statistics.delay.DelayStatService;
import com.alibaba.otter.manager.biz.statistics.throughput.ThroughputStatService;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputCondition;
import com.alibaba.otter.shared.arbitrate.ArbitrateViewService;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.arbitrate.model.PositionEventData;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.statistics.delay.DelayStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputType;

public class PipelineList {

    @Resource(name = "channelService")
    private ChannelService        channelService;

    @Resource(name = "delayStatService")
    private DelayStatService      delayStatService;

    @Resource(name = "arbitrateViewService")
    private ArbitrateViewService  arbitrateViewService;

    @Resource(name = "throughputStatService")
    private ThroughputStatService throughputStatService;

    @Resource(name = "alarmRuleService")
    private AlarmRuleService      alarmRuleService;

    public void execute(@Param("channelId") Long channelId, @Param("pipelineId") Long pipelineId, HttpSession session,
                        Context context) throws Exception {

        Channel channel = channelService.findByIdWithoutColumn(channelId);
        List<Pipeline> pipelines = channel.getPipelines();
        List<Pipeline> tempPipe = new ArrayList<Pipeline>();

        if ((pipelineId != null) && (pipelineId != 0l)) {
            for (Pipeline pipeline : pipelines) {
                if (!pipeline.getId().equals(pipelineId)) {
                    tempPipe.add(pipeline);
                }
            }
            pipelines.removeAll(tempPipe);
        }

        Map<Long, DelayStat> delayStats = new HashMap<Long, DelayStat>(pipelines.size(), 1f);
        Map<Long, MainStemEventData> mainstemDatas = new HashMap<Long, MainStemEventData>(pipelines.size(), 1f);
        Map<Long, ThroughputStat> throughputStats = new HashMap<Long, ThroughputStat>(pipelines.size(), 1f);
        Map<Long, List<AlarmRule>> alarmRuleStats = new HashMap<Long, List<AlarmRule>>(pipelines.size(), 1f);
        Map<Long, PositionEventData> positionDatas = new HashMap<Long, PositionEventData>(pipelines.size(), 1f);
        for (Pipeline pipeline : pipelines) {
            DelayStat delayStat = delayStatService.findRealtimeDelayStat(pipeline.getId());
            if (delayStat.getDelayNumber() == null) {
                delayStat.setDelayNumber(0L);
                delayStat.setDelayTime(0L);
                delayStat.setGmtModified(pipeline.getGmtModified());
            }
            delayStats.put(pipeline.getId(), delayStat);
            mainstemDatas.put(pipeline.getId(), arbitrateViewService.mainstemData(channel.getId(), pipeline.getId()));
            ThroughputCondition condition = new ThroughputCondition();
            condition.setPipelineId(pipeline.getId());
            condition.setType(ThroughputType.ROW);
            ThroughputStat throughputStat = throughputStatService.findThroughputStatByPipelineId(condition);
            throughputStats.put(pipeline.getId(), throughputStat);
            List<AlarmRule> alarmRules = alarmRuleService.getAlarmRules(pipeline.getId());
            alarmRuleStats.put(pipeline.getId(), alarmRules);
            PositionEventData positionData = arbitrateViewService.getCanalCursor(pipeline.getParameters().getDestinationName(),
                                                                                 pipeline.getParameters().getMainstemClientId());
            positionDatas.put(pipeline.getId(), positionData);
        }

        context.put("channel", channel);
        context.put("pipelines", pipelines);
        context.put("delayStats", delayStats);
        context.put("throughputStats", throughputStats);
        context.put("alarmRuleStats", alarmRuleStats);
        context.put("mainstemDatas", mainstemDatas);
        context.put("positionDatas", positionDatas);
    }
}
