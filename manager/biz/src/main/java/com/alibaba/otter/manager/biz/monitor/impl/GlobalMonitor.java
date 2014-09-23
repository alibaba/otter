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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.manager.biz.config.alarm.AlarmRuleService;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.monitor.AlarmRecovery;
import com.alibaba.otter.manager.biz.monitor.Monitor;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRuleStatus;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;

/**
 * 监控机制实现，不会自动触发，需要外部配置定时任务。 <br/>
 * 调用http://xxxx/monitor/monitor_trigger.htm?token=otter来进行触发，主要考虑otter集群，只需要有一个监控运行即可，如果jvm自运行，会同时并行监控
 * 
 * @author zebin.xuzb @ 2012-8-23
 * @version 4.1.0
 */
public class GlobalMonitor implements Monitor, InitializingBean, DisposableBean {

    protected static final Logger  log             = LoggerFactory.getLogger("monitorInfo");
    private static final int       DEFAULT_THREADS = 5;

    private int                    nThreads;
    private boolean                needConcurrent  = true;
    private boolean                recoveryPaused  = true;
    private ExecutorService        executor;

    private AlarmRuleService       alarmRuleService;
    private Monitor                pipelineMonitor;

    private ChannelService         channelService;
    private ArbitrateManageService arbitrateManageService;
    private AlarmRecovery          restartAlarmRecovery;

    @Override
    public void explore() {
        Map<Long, List<AlarmRule>> rules = alarmRuleService.getAlarmRules(AlarmRuleStatus.ENABLE);
        if (!CollectionUtils.isEmpty(rules)) {
            if (needConcurrent) {
                concurrentProcess(rules);
            } else {// 串行
                serialProcess(rules);
            }
        } else {
            log.warn("no enabled alarm rule at all. Check the rule setting please!");
        }

        // 自动恢复机制
        if (recoveryPaused) {
            List<Long> channelIds = channelService.listAllChannelId();
            if (needConcurrent) {
                concurrentProcess(channelIds);
            } else {// 串行
                serialProcess(channelIds);
            }
        }
    }

    private void concurrentProcess(Map<Long, List<AlarmRule>> rules) {
        ExecutorCompletionService completionExecutor = new ExecutorCompletionService(executor);
        List<Future> futures = new ArrayList<Future>();
        for (Entry<Long, List<AlarmRule>> entry : rules.entrySet()) {
            final List<AlarmRule> alarmRules = entry.getValue();
            futures.add(completionExecutor.submit(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    pipelineMonitor.explore(alarmRules);
                    return null;
                }
            }));
        }

        List<Throwable> exceptions = new ArrayList<Throwable>();
        int index = 0;
        int size = futures.size();
        while (index < size) {
            try {
                Future<?> future = completionExecutor.take();
                future.get();
            } catch (InterruptedException e) {
                exceptions.add(e);
            } catch (ExecutionException e) {
                exceptions.add(e);
            }
            index++;
        }

        if (!exceptions.isEmpty()) {
            StringBuilder sb = new StringBuilder(exceptions.size() + " exception happens in global monitor\n");
            sb.append("exception stack start :\n");
            for (Throwable t : exceptions) {
                sb.append(ExceptionUtils.getStackTrace(t));
            }
            sb.append("exception stack end \n");
            throw new IllegalStateException(sb.toString());
        }
    }

    private void serialProcess(Map<Long, List<AlarmRule>> rules) {
        for (Entry<Long, List<AlarmRule>> entry : rules.entrySet()) {
            List<AlarmRule> alarmRules = entry.getValue();
            pipelineMonitor.explore(alarmRules);
        }
    }

    private void concurrentProcess(List<Long> channelIds) {
        ExecutorCompletionService completionExecutor = new ExecutorCompletionService(executor);
        List<Future> futures = new ArrayList<Future>();
        for (final Long channelId : channelIds) {
            futures.add(completionExecutor.submit(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    ChannelStatus status = arbitrateManageService.channelEvent().status(channelId);
                    if (status.isPause()) {
                        restartAlarmRecovery.recovery(channelId);
                    }
                    return null;
                }
            }));
        }

        List<Throwable> exceptions = new ArrayList<Throwable>();
        int index = 0;
        int size = futures.size();
        while (index < size) {
            try {
                Future<?> future = completionExecutor.take();
                future.get();
            } catch (InterruptedException e) {
                exceptions.add(e);
            } catch (ExecutionException e) {
                exceptions.add(e);
            }
            index++;
        }

        if (!exceptions.isEmpty()) {
            StringBuilder sb = new StringBuilder(exceptions.size() + " exception happens in global monitor\n");
            sb.append("exception stack start :\n");
            for (Throwable t : exceptions) {
                sb.append(ExceptionUtils.getStackTrace(t));
            }
            sb.append("exception stack end \n");
            throw new IllegalStateException(sb.toString());
        }
    }

    private void serialProcess(List<Long> channelIds) {
        for (Long channelId : channelIds) {
            ChannelStatus status = arbitrateManageService.channelEvent().status(channelId);
            if (status.isPause()) {
                restartAlarmRecovery.recovery(channelId);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        nThreads = nThreads <= 0 ? DEFAULT_THREADS : nThreads;
        executor = new ThreadPoolExecutor(nThreads, nThreads, 0, TimeUnit.MILLISECONDS,
                                          new LinkedBlockingQueue<Runnable>(nThreads * 2),
                                          new NamedThreadFactory("global monitor", false),
                                          new ThreadPoolExecutor.CallerRunsPolicy());

    }

    @Override
    public void destroy() throws Exception {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    @Override
    public void explore(Long... pipelineIds) {
        throw new UnsupportedOperationException("doesn't support right now");
    }

    @Override
    public void explore(List<AlarmRule> rules) {
        throw new UnsupportedOperationException("doesn't support right now");
    }

    // ============== setter ==============
    public void setnThreads(int nThreads) {
        this.nThreads = nThreads;
    }

    public void setNeedConcurrent(boolean needConcurrent) {
        this.needConcurrent = needConcurrent;
    }

    public void setAlarmRuleService(AlarmRuleService alarmRuleService) {
        this.alarmRuleService = alarmRuleService;
    }

    public void setPipelineMonitor(Monitor pipelineMonitor) {
        this.pipelineMonitor = pipelineMonitor;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public void setArbitrateManageService(ArbitrateManageService arbitrateManageService) {
        this.arbitrateManageService = arbitrateManageService;
    }

    public void setRestartAlarmRecovery(AlarmRecovery restartAlarmRecovery) {
        this.restartAlarmRecovery = restartAlarmRecovery;
    }

    public void setRecoveryPaused(boolean recoveryPaused) {
        this.recoveryPaused = recoveryPaused;
    }

}
