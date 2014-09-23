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
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.biz.statistics.table.TableStatService;
import com.alibaba.otter.manager.biz.statistics.table.param.BehaviorHistoryInfo;
import com.alibaba.otter.manager.biz.statistics.table.param.TimelineBehaviorHistoryCondition;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;

public class BehaviorHistoryCurve {

    @Resource(name = "channelService")
    private ChannelService       channelService;

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService dataMediaPairService;

    @Resource(name = "tableStatService")
    private TableStatService     tableStatService;

    public void execute(@Param("d5221") String startTime, @Param("d5222") String endTime,
                        @Param("dataMediaPairId") Long dataMediaPairId, HttpSession session, Context context)
                                                                                                             throws Exception {
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

        DataMediaPair dataMediaPair = dataMediaPairService.findById(dataMediaPairId);
        Channel channel = channelService.findByPipelineId(dataMediaPair.getPipelineId());

        Map<Long, BehaviorHistoryInfo> behaviourHistoryInfos = new LinkedHashMap<Long, BehaviorHistoryInfo>();

        TimelineBehaviorHistoryCondition condition = new TimelineBehaviorHistoryCondition();

        if (null != start && null != end) {
            condition.setStart(start);
            condition.setEnd(end);
            condition.setPairId(dataMediaPairId);
            behaviourHistoryInfos = tableStatService.listTimelineBehaviorHistory(condition);
        }

        Long totalInsert = 0L;
        Long totalUpdate = 0L;
        Long totalDelete = 0L;
        Long totalFileCount = 0L;
        Long totalFileSize = 0L;
        for (BehaviorHistoryInfo info : behaviourHistoryInfos.values()) {
            totalInsert += info.getInsertNumber();
            totalUpdate += info.getUpdateNumber();
            totalDelete += info.getDeleteNumber();
            totalFileCount += info.getFileNumber();
            totalFileSize += info.getFileSize();
        }

        context.put("totalInsert", totalInsert);
        context.put("totalUpdate", totalUpdate);
        context.put("totalDelete", totalDelete);
        context.put("totalFileCount", totalFileCount);
        context.put("totalFileSize", totalFileSize);
        context.put("behaviourHistoryInfos", behaviourHistoryInfos);
        context.put("start", sdf.format(start));
        context.put("end", sdf.format(end));
        context.put("dataMediaPair", dataMediaPair);
        context.put("channel", channel);
    }
}
