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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.statistics.throughput.ThroughputStatService;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputInfo;
import com.alibaba.otter.manager.biz.statistics.throughput.param.TimelineThroughputCondition;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputType;

public class AnalysisThroughputHistory {

    @Resource(name = "channelService")
    private ChannelService        channelService;

    @Resource(name = "throughputStatService")
    private ThroughputStatService throughputStatService;

    public void execute(@Param("d5221") String startTime, @Param("d5222") String endTime,
                        @Param("pipelineId") Long pipelineId, HttpSession session, Context context) throws Exception {
        Date end = null;
        Date start = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            start = new Date(System.currentTimeMillis() / 60000 * 60000 - 24 * 60 * 60 * 1000);
            end = new Date(System.currentTimeMillis() / 60000 * 60000);
        } else {// 当前24小时，时间取整分钟
            sdf.setLenient(false);
            if (null != startTime && null != endTime) {
                start = sdf.parse(startTime);
                end = sdf.parse(endTime);
            }
        }

        Channel channel = channelService.findByPipelineId(pipelineId);
        Map<Long, ThroughputInfo> throughputInfos1 = new LinkedHashMap<Long, ThroughputInfo>();
        Map<Long, ThroughputInfo> throughputInfos2 = new LinkedHashMap<Long, ThroughputInfo>();
        TimelineThroughputCondition condition1 = new TimelineThroughputCondition();
        TimelineThroughputCondition condition2 = new TimelineThroughputCondition();
        if (null != start && null != end) {
            condition1.setStart(start);
            condition1.setEnd(end);
            condition1.setType(ThroughputType.ROW);
            condition1.setPipelineId(pipelineId);
            condition2.setStart(start);
            condition2.setEnd(end);
            condition2.setType(ThroughputType.FILE);
            condition2.setPipelineId(pipelineId);
            throughputInfos1 = throughputStatService.listTimelineThroughput(condition1);
            throughputInfos2 = throughputStatService.listTimelineThroughput(condition2);
        }

        Long totalRecord1 = 0L;
        Long totalRecord2 = 0L;
        Long totalSize1 = 0L;
        Long totalSize2 = 0L;
        for (ThroughputInfo info : throughputInfos1.values()) {
            totalRecord1 += info.getNumber();
            totalSize1 += info.getSize();
        }

        for (ThroughputInfo info : throughputInfos2.values()) {
            totalRecord2 += info.getNumber();
            totalSize2 += info.getSize();
        }

        context.put("throughputInfos1", throughputInfos1);
        context.put("throughputInfos2", throughputInfos2);
        context.put("totalRecord1", totalRecord1);
        context.put("totalRecord2", totalRecord2);
        context.put("totalSize1", totalSize1);
        context.put("totalSize2", totalSize2);
        context.put("channel", channel);
        context.put("pipelineId", pipelineId);
        context.put("start", sdf.format(start));
        context.put("end", sdf.format(end));
    }
}
