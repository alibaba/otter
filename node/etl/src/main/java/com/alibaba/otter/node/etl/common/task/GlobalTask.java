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

package com.alibaba.otter.node.etl.common.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.common.jmx.StageAggregationCollector;
import com.alibaba.otter.node.etl.common.pipe.impl.RowDataPipeDelegate;
import com.alibaba.otter.shared.arbitrate.ArbitrateEventService;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * mainstem,select,extract,transform,load parent Thread.
 * 
 * @author xiaoqing.zhouxq 2011-8-23 上午10:38:14
 */
public abstract class GlobalTask extends Thread {

    protected final Logger              logger  = LoggerFactory.getLogger(this.getClass());
    protected volatile boolean          running = true;
    protected Pipeline                  pipeline;
    protected Long                      pipelineId;
    protected ArbitrateEventService     arbitrateEventService;
    protected RowDataPipeDelegate       rowDataPipeDelegate;
    protected ExecutorService           executorService;
    protected ConfigClientService       configClientService;
    protected StageAggregationCollector stageAggregationCollector;
    protected Map<Long, Future>         pendingFuture;

    public GlobalTask(Pipeline pipeline){
        this(pipeline.getId());
        this.pipeline = pipeline;
    }

    public GlobalTask(Long pipelineId){
        this.pipelineId = pipelineId;
        setName(createTaskName(pipelineId, ClassUtils.getShortClassName(this.getClass())));
        pendingFuture = new HashMap<Long, Future>();
    }

    public void shutdown() {
        running = false;
        interrupt();

        List<Future> cancelFutures = new ArrayList<Future>();
        for (Map.Entry<Long, Future> entry : pendingFuture.entrySet()) {
            if (!entry.getValue().isDone()) {
                logger.warn("WARN ## Task future processId[{}] canceled!", entry.getKey());
                cancelFutures.add(entry.getValue());
            }
        }

        for (Future future : cancelFutures) {
            future.cancel(true);
        }
        pendingFuture.clear();
    }

    protected void sendRollbackTermin(long pipelineId, Throwable exception) {
        sendRollbackTermin(pipelineId, ExceptionUtils.getFullStackTrace(exception));
    }

    protected void sendRollbackTermin(long pipelineId, String message) {
        TerminEventData errorEventData = new TerminEventData();
        errorEventData.setPipelineId(pipelineId);
        errorEventData.setType(TerminType.ROLLBACK);
        errorEventData.setCode("setl");
        errorEventData.setDesc(message);
        arbitrateEventService.terminEvent().single(errorEventData);
        // 每次发送完报警后，sleep一段时间，继续做后面的事
        try {
            Thread.sleep(3000 + RandomUtils.nextInt(3000));
        } catch (InterruptedException e) {
        }
    }

    /**
     * 自动处理数据为null的情况，重新发一遍数据
     */
    protected void processMissData(long pipelineId, String message) {
        TerminEventData errorEventData = new TerminEventData();
        errorEventData.setPipelineId(pipelineId);
        errorEventData.setType(TerminType.RESTART);
        errorEventData.setCode("setl");
        errorEventData.setDesc(message);
        arbitrateEventService.terminEvent().single(errorEventData);
    }

    protected String createTaskName(long pipelineId, String taskName) {
        return new StringBuilder().append("pipelineId = ").append(pipelineId).append(",taskName = ").append(taskName).toString();
    }

    protected boolean isProfiling() {
        return stageAggregationCollector.isProfiling();
    }

    protected boolean isInterrupt(Throwable e) {
        if (!running) {
            return true;
        }

        if (e instanceof InterruptedException || e instanceof ZkInterruptedException) {
            return true;
        }

        if (ExceptionUtils.getRootCause(e) instanceof InterruptedException) {
            return true;
        }

        return false;

    }

    public Collection<Long> getPendingProcess() {
        List<Long> result = new ArrayList<Long>(pendingFuture.keySet());
        Collections.sort(result);
        return result;
    }

    // ====================== setter / getter =========================

    public void setArbitrateEventService(ArbitrateEventService arbitrateEventService) {
        this.arbitrateEventService = arbitrateEventService;
    }

    public void setRowDataPipeDelegate(RowDataPipeDelegate rowDataPipeDelegate) {
        this.rowDataPipeDelegate = rowDataPipeDelegate;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setStageAggregationCollector(StageAggregationCollector stageAggregationCollector) {
        this.stageAggregationCollector = stageAggregationCollector;
    }

}
