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

package com.alibaba.otter.manager.biz.monitor.impl;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.monitor.AlarmRecovery;
import com.alibaba.otter.manager.biz.monitor.PassiveMonitor;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.communication.model.arbitrate.NodeAlarmEvent;

/**
 * 基于RESTART命令的恢复机制
 * 
 * @author jianghang 2012-9-19 下午04:44:12
 * @version 4.1.0
 */
public class RestartAlarmRecovery implements AlarmRecovery, InitializingBean, DisposableBean {

    private static final Logger                       logger    = LoggerFactory.getLogger(RestartAlarmRecovery.class);
    private volatile DelayQueue<AlarmRecoveryDelayed> queue     = new DelayQueue<AlarmRecoveryDelayed>();
    private long                                      checkTime = 10 * 1000L;                                         // 5秒
    private ExecutorService                           executor;
    private PipelineService                           pipelineService;
    private PassiveMonitor                            exceptionRuleMonitor;
    private ArbitrateManageService                    arbitrateManageService;
    private ChannelService                            channelService;

    public void recovery(Long channelId) {
        AlarmRecoveryDelayed delayed = new AlarmRecoveryDelayed(channelId, -1, false, checkTime);
        // 做异步处理，避免并发时重复执行recovery
        synchronized (queue) {
            if (!queue.contains(delayed)) {
                queue.add(delayed);
            }
        }
    }

    public void recovery(AlarmRule alarmRule) {
        Pipeline pipeline = pipelineService.findById(alarmRule.getPipelineId());
        AlarmRecoveryDelayed delayed = new AlarmRecoveryDelayed(pipeline.getChannelId(), alarmRule.getId(), false,
                                                                checkTime);
        // 做异步处理，避免并发时重复执行recovery
        synchronized (queue) {
            if (!queue.contains(delayed)) {
                queue.add(delayed);
            }
        }
    }

    public void recovery(AlarmRule alarmRule, long alarmCount) {
        if (alarmCount >= alarmRule.getRecoveryThresold()) {
            synchronized (queue) {
                // 做异步处理，避免并发时重复执行recovery
                Pipeline pipeline = pipelineService.findById(alarmRule.getPipelineId());
                // 超过2倍阀值，强制停止一下通道释放一下内存
                boolean needStop = (alarmCount >= alarmRule.getRecoveryThresold() + 1);// recovery的下一次启用修复
                AlarmRecoveryDelayed delayed = new AlarmRecoveryDelayed(pipeline.getChannelId(), alarmRule.getId(),
                                                                        needStop, checkTime);
                if (!queue.contains(delayed)) {
                    queue.add(delayed);
                }
            }
        }
    }

    private boolean processRecovery(Long channelId, Long ruleId, boolean needStop) {
        boolean result = true;
        if (!needStop) {
            result = arbitrateManageService.channelEvent().restart(channelId);
            if (result) {
                channelService.notifyChannel(channelId);// 推送一下配置
            }
        } else {
            // 解决process rpc模式下释放不完整，通过stop完整释放一次所有对象资源
            channelService.stopChannel(channelId);
            channelService.startChannel(channelId);
        }

        NodeAlarmEvent alarm = new NodeAlarmEvent();
        alarm.setPipelineId(-1L);
        alarm.setTitle(MonitorName.EXCEPTION.name());
        if (result) {
            if (!needStop) {
                alarm.setMessage(String.format("cid:%s restart recovery successful for rid:%s",
                                               String.valueOf(channelId), String.valueOf(ruleId)));
            } else {
                alarm.setMessage(String.format("cid:%s stop recovery successful for rid:%s", String.valueOf(channelId),
                                               String.valueOf(ruleId)));
            }

            try {
                exceptionRuleMonitor.feed(alarm, alarm.getPipelineId());
            } catch (Exception e) {
                logger.error(String.format("ERROR # exceptionRuleMonitor error for %s", alarm.toString()), e);
            }
        }

        return result;
    }

    public void afterPropertiesSet() throws Exception {
        executor = Executors.newFixedThreadPool(1);
        executor.submit(new Runnable() {

            public void run() {
                while (true) {
                    AlarmRecoveryDelayed delay = null;
                    try {
                        delay = queue.take();
                        processRecovery(delay.getChannelId(), delay.getRuleId(), delay.isStop());
                    } catch (Throwable e) {
                        // 出错了，重新加入补救处理
                        if (!queue.contains(delay)) {
                            queue.add(delay);
                        }
                        logger.error(String.format("error happened with [%s]", delay.toString()), e);
                    }
                }
            }
        });
    }

    public void destroy() throws Exception {
        executor.shutdownNow();
    }

    public void setArbitrateManageService(ArbitrateManageService arbitrateManageService) {
        this.arbitrateManageService = arbitrateManageService;
    }

    public void setPipelineService(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    public void setExceptionRuleMonitor(PassiveMonitor exceptionRuleMonitor) {
        this.exceptionRuleMonitor = exceptionRuleMonitor;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

}
