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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.statistics.stage.ProcessStatService;
import com.alibaba.otter.shared.arbitrate.ArbitrateViewService;
import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.arbitrate.model.PositionEventData;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.statistics.stage.ProcessStat;

public class AnalysisStageStat {

    @Resource(name = "pipelineService")
    private PipelineService       pipelineService;

    @Resource(name = "processStatService")
    private ProcessStatService    processStatService;

    @Resource(name = "arbitrateViewService")
    private ArbitrateViewService  arbitrateViewService;

    @Resource(name = "channelEvent")
    private ChannelArbitrateEvent channelArbitrateEvent;

    public void execute(@Param("pipelineId") Long pipelineId, Context context) throws Exception {

        List<ProcessStat> processStats = new ArrayList<ProcessStat>();
        Pipeline pipeline = pipelineService.findById(pipelineId);
        processStats = processStatService.listRealtimeProcessStat(pipelineId);
        // Map ext = new HashMap<Long, String>();
        // // ext.put(145456451, "asdf");
        // for (Long i = 1L; i <= 3; i++) {
        // List<StageStat> stageStats = new ArrayList<StageStat>();
        // ProcessStat processStat = new ProcessStat();
        // processStat.setPipelineId(1L);
        // processStat.setProcessId(i);
        // StageStat stage = new StageStat();
        // stage.setStage(StageType.SELECT);
        // stage.setStartTime(((new Date()).getTime() + i * 10 * 1000));
        // stage.setEndTime(((new Date()).getTime() + i * 200 * 1000));
        // stage.setNumber(11231230L);
        // stage.setSize(14545645640L);
        // // stage.setExts(ext);
        // stageStats.add(stage);
        // stage = new StageStat();
        // stage.setStage(StageType.EXTRACT);
        // stage.setStartTime(((new Date()).getTime() + i * 2000 * 1000));
        // stage.setEndTime(((new Date()).getTime() + i * 3000 * 1000));
        // stage.setExts(ext);
        // // stage.setNumber(10L);
        // // stage.setSize(10L);
        // stageStats.add(stage);
        // stage = new StageStat();
        // stage.setStage(StageType.TRANSFORM);
        // stage.setStartTime(((new Date()).getTime() + i * 5000 * 1000));
        // stage.setEndTime(((new Date()).getTime() + i * 6000 * 1000));
        // stage.setNumber(154640L);
        // stage.setExts(ext);
        // // stage.setSize(10L);
        // stageStats.add(stage);
        // stage = new StageStat();
        // stage.setStage(StageType.LOAD);
        // stage.setStartTime(((new Date()).getTime() + i * 70000 * 1000));
        // stage.setEndTime(((new Date()).getTime() + i * 80000 * 1000));
        // // stage.setNumber(10L);
        // stage.setSize(101445L);
        // // stage.setExts(ext);
        // stageStats.add(stage);
        // processStat.setStageStats(stageStats);
        // processStats.add(processStat);
        // }

        Long stageStart = 0L;
        // Long stageEnd = new Date().getTime() + 3 * 80000 * 1000;
        Long stageEnd = new Date().getTime();
        Long interval = 0L;
        double offset = 0L;
        // 找出最先开始的process的select阶段的开始时间作为起始时间
        if (processStats.size() > 0) {
            if (processStats.get(0).getStageStats().size() > 0) {
                stageStart = processStats.get(0).getStageStats().get(0).getStartTime();
            }
        }

        // 动态计算每个阶段的长度比例
        if (stageStart > 0) {
            interval = stageEnd - stageStart;
        }
        if (interval > 0) {
            offset = 800.0 / interval;
        }

        // 计算每个process当前任务所做的时间总和
        Map<Long, Long> processTime = new HashMap<Long, Long>();
        for (ProcessStat processStat : processStats) {
            Long timeout = 0L;
            if (processStat.getStageStats().size() > 0) {
                timeout = stageEnd - processStat.getStageStats().get(0).getStartTime();
            }
            processTime.put(processStat.getProcessId(), timeout);
        }

        // 获取下mainstem状态信息
        MainStemEventData mainstemData = arbitrateViewService.mainstemData(pipeline.getChannelId(), pipelineId);

        PositionEventData positionData = arbitrateViewService.getCanalCursor(pipeline.getParameters().getDestinationName(),
                                                                             pipeline.getParameters().getMainstemClientId());

        ChannelStatus status = channelArbitrateEvent.status(pipeline.getChannelId());

        context.put("pipeline", pipeline);
        context.put("pipelineId", pipelineId);
        context.put("processStats", processStats);
        context.put("offset", offset);
        context.put("stageStart", stageStart);
        context.put("stageEnd", stageEnd);
        context.put("processTime", processTime);
        context.put("mainstemData", mainstemData);
        context.put("positionData", positionData);
        context.put("channelStatus", status);
    }
}
