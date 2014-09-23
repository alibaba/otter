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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.statistics.delay.DelayStatService;
import com.alibaba.otter.manager.biz.statistics.delay.param.DelayStatInfo;
import com.alibaba.otter.shared.common.model.config.channel.Channel;

public class AnalysisDelayStat {

    @Resource(name = "channelService")
    private ChannelService   channelService;

    @Resource(name = "delayStatService")
    private DelayStatService delayStatService;

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
        Map<Long, DelayStatInfo> delayStatInfos = new HashMap<Long, DelayStatInfo>();
        if (null != start && null != end) {
            delayStatInfos = delayStatService.listTimelineDelayStat(pipelineId, start, end);
        }

        Double delayAvg = 0.0;
        for (DelayStatInfo info : delayStatInfos.values()) {
            delayAvg += info.getAvgDelayTime();
        }

        if (delayStatInfos.size() != 0) {
            delayAvg = delayAvg / (1.0 * delayStatInfos.size());
        }

        context.put("delayStatInfos", delayStatInfos);
        context.put("delayAvg", delayAvg);
        context.put("channel", channel);
        context.put("pipelineId", pipelineId);
        context.put("start", sdf.format(start));
        context.put("end", sdf.format(end));
    }
}
