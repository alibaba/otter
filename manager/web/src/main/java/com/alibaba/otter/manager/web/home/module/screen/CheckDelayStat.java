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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.statistics.delay.DelayStatService;
import com.alibaba.otter.manager.biz.statistics.throughput.ThroughputStatService;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputCondition;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.statistics.delay.DelayStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputType;

/**
 * 类CheckQueueSize.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-12-30 下午04:01:17
 */
public class CheckDelayStat {

    private static final Log      logger      = LogFactory.getLog(CheckDelayStat.class);

    @Resource(name = "delayStatService")
    private DelayStatService      delayStatService;

    @Resource(name = "throughputStatService")
    private ThroughputStatService throughputStatService;

    @Resource(name = "channelService")
    private ChannelService        channelService;

    private static final int      MAX_TIMEOUT = 30;                                     // 超时的最大时间，单位分钟

    private static Map<Long, Long> parseAlert(String alert) {
        if (alert == null) {
            return null;
        }

        Map<Long, Long> alertMap = new HashMap<Long, Long>();
        String[] alerts = alert.split(",");

        for (int i = 0; i < alerts.length; i++) {
            String[] ncidAlert = alerts[i].split("-");

            alertMap.put(NumberUtils.toLong(ncidAlert[0], 0), NumberUtils.toLong(ncidAlert[1], 0));

            if (logger.isInfoEnabled()) {
                logger.info(ncidAlert[0] + " : " + ncidAlert[1]);
            }
        }

        return alertMap;
    }

    public static void main(String[] args) {
        Map<Long, Long> alertMap = parseAlert("20-1000000, 30-20000");

        for (Long pipelineId : alertMap.keySet()) {
            System.out.println(pipelineId + " : " + alertMap.get(pipelineId));
        }
    }

    public void execute(@Param("queueSize") String queueSize, @Param("delayTime") String delayTime,
                        @Param("timeout") String timeout, Context context) throws WebxException {

        Map<Long, Long> queueSizeMap = parseAlert(queueSize);
        Map<Long, Long> delayTimeMap = parseAlert(delayTime);
        Map<Long, Long> timeoutMap = parseAlert(timeout);
        Boolean result = true;

        if ((queueSizeMap != null) && (false == queueSizeMap.isEmpty())) {
            Set<Long> key = queueSizeMap.keySet();
            for (Iterator it = key.iterator(); it.hasNext();) {
                Long pipelineId = (Long) it.next();

                Channel channel = channelService.findByPipelineId(pipelineId);
                // 判断channel状态，只有启动状态才进行判断超时时间
                if (!channel.getStatus().isStop()) {

                    DelayStat delayStat = delayStatService.findRealtimeDelayStat(pipelineId);
                    logger.info("delayStat.getDelayNumber() == " + delayStat.getDelayNumber());

                    if (null != delayStat.getDelayNumber()
                        && delayStat.getDelayNumber() >= queueSizeMap.get(pipelineId)) {
                        result = false;
                    }
                }
            }
        }
        if ((delayTimeMap != null) && (false == delayTimeMap.isEmpty())) {
            Set<Long> key = delayTimeMap.keySet();
            for (Iterator it = key.iterator(); it.hasNext();) {
                Long pipelineId = (Long) it.next();
                Channel channel = channelService.findByPipelineId(pipelineId);
                // 判断channel状态，只有启动状态才进行判断超时时间
                if (!channel.getStatus().isStop()) {
                    DelayStat delayStat = delayStatService.findRealtimeDelayStat(pipelineId);
                    logger.info("delayStat.getDelayTime() == " + delayStat.getDelayTime());

                    if (null != delayStat.getDelayTime() && delayStat.getDelayTime() >= delayTimeMap.get(pipelineId)) {
                        result = false;
                    }
                }
            }
        }

        if ((timeoutMap != null) && (false == timeoutMap.isEmpty())) {
            Set<Long> key = timeoutMap.keySet();
            for (Iterator it = key.iterator(); it.hasNext();) {
                Long pipelineId = (Long) it.next();
                Channel channel = channelService.findByPipelineId(pipelineId);
                // 判断channel状态，只有启动状态才进行判断超时时间
                if (!channel.getStatus().isStop()) {
                    ThroughputCondition condition = new ThroughputCondition();
                    condition.setPipelineId(pipelineId);
                    condition.setType(ThroughputType.ROW);
                    ThroughputStat throughputStat = throughputStatService.findThroughputStatByPipelineId(condition);

                    if (null != throughputStat.getGmtModified()) {
                        Date now = new Date();
                        long time = now.getTime() - throughputStat.getGmtModified().getTime();
                        logger.info("timeout == " + time + "(ms)");

                        long timeout_min = MAX_TIMEOUT; // 单位为分钟
                        if (timeoutMap.containsKey(pipelineId)) {
                            timeout_min = timeoutMap.get(pipelineId);
                        }
                        if (time / (1000 * 60) > timeout_min) {
                            result = false;
                        }
                    }
                }
            }
        }
        context.put("result", result);

    }
}
