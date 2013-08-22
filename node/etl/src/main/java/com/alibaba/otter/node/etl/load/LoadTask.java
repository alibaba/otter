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

package com.alibaba.otter.node.etl.load;

import java.util.List;

import org.slf4j.MDC;

import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.common.jmx.StageAggregation.AggregationItem;
import com.alibaba.otter.node.etl.common.pipe.PipeKey;
import com.alibaba.otter.node.etl.common.task.GlobalTask;
import com.alibaba.otter.node.etl.extract.SetlFuture;
import com.alibaba.otter.node.etl.load.loader.LoadContext;
import com.alibaba.otter.node.etl.load.loader.OtterLoaderFactory;
import com.alibaba.otter.node.etl.load.loader.db.context.DbLoadContext;
import com.alibaba.otter.node.etl.load.loader.interceptor.LoadInterceptor;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.etl.model.DbBatch;

/**
 * load工作线程,负责桥接连接仲裁器,Config,loader
 * 
 * @author jianghang 2011-11-3 下午07:05:20
 * @version 4.0.0
 */
public class LoadTask extends GlobalTask {

    private OtterLoaderFactory otterLoaderFactory;
    private LoadInterceptor    dbLoadInterceptor;

    public LoadTask(Long pipelineId){
        super(pipelineId);
    }

    public void run() {
        MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipelineId));
        while (running) {
            try {
                final EtlEventData etlEventData = arbitrateEventService.loadEvent().await(pipelineId);
                Runnable task = new Runnable() {

                    public void run() {
                        // 设置profiling信息
                        boolean profiling = isProfiling();
                        Long profilingStartTime = null;
                        if (profiling) {
                            profilingStartTime = System.currentTimeMillis();
                        }

                        MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipelineId));
                        String currentName = Thread.currentThread().getName();
                        Thread.currentThread().setName(createTaskName(pipelineId, "LoadWorker"));
                        List<LoadContext> processedContexts = null;
                        try {
                            // 后续可判断同步数据是否为rowData
                            List<PipeKey> keys = (List<PipeKey>) etlEventData.getDesc();
                            DbBatch dbBatch = rowDataPipeDelegate.get(keys);

                            // 可能拿到为null，因为内存不足或者网络异常，长时间阻塞时，导致从pipe拿数据出现异常，数据可能被上一个节点已经删除
                            if (dbBatch == null) {
                                processMissData(pipelineId, "load miss data with keys:" + keys.toString());
                                return;
                            }

                            // 进行数据load处理
                            otterLoaderFactory.setStartTime(dbBatch.getRowBatch().getIdentity(),
                                                            etlEventData.getStartTime());

                            processedContexts = otterLoaderFactory.load(dbBatch);

                            if (profiling) {
                                Long profilingEndTime = System.currentTimeMillis();
                                stageAggregationCollector.push(pipelineId,
                                                               StageType.LOAD,
                                                               new AggregationItem(profilingStartTime, profilingEndTime));
                            }
                            // 处理完成后通知single已完成
                            arbitrateEventService.loadEvent().single(etlEventData);
                        } catch (Throwable e) {
                            if (!isInterrupt(e)) {
                                logger.error(String.format("[%s] loadWork executor is error! data:%s", pipelineId,
                                                           etlEventData), e);
                            } else {
                                logger.info(String.format("[%s] loadWork executor is interrrupt! data:%s", pipelineId,
                                                          etlEventData), e);
                            }

                            if (processedContexts != null) {// 说明load成功了，但是通知仲裁器失败了，需要记录下记录到store
                                for (LoadContext context : processedContexts) {
                                    try {
                                        if (context instanceof DbLoadContext) {
                                            dbLoadInterceptor.error((DbLoadContext) context);
                                        }

                                    } catch (Throwable ie) {
                                        logger.error(String.format("[%s] interceptor process error failed!", pipelineId),
                                                     ie);
                                    }
                                }
                            }

                            // try {
                            // arbitrateEventService.loadEvent().release(pipelineId);
                            // // 释放锁
                            // } catch (Throwable ie) {
                            // logger.error(String.format("[%s] load release failed!",
                            // pipelineId), ie);
                            // }

                            if (!isInterrupt(e)) {
                                sendRollbackTermin(pipelineId, e);
                            }
                        } finally {
                            Thread.currentThread().setName(currentName);
                            MDC.remove(OtterConstants.splitPipelineLogFileKey);
                        }
                    }
                };

                // 构造pending任务，可在关闭线程时退出任务
                SetlFuture extractFuture = new SetlFuture(StageType.LOAD, etlEventData.getProcessId(), pendingFuture,
                                                          task);
                executorService.execute(extractFuture);
            } catch (Throwable e) {
                if (isInterrupt(e)) {
                    logger.info(String.format("[%s] loadTask is interrupted!", pipelineId), e);
                    // arbitrateEventService.loadEvent().release(pipelineId); //
                    // 释放锁
                    return;
                } else {
                    logger.error(String.format("[%s] loadTask is error!", pipelineId), e);
                    // arbitrateEventService.loadEvent().release(pipelineId); //
                    // 释放锁
                    sendRollbackTermin(pipelineId, e); // 先解除lock，后发送rollback信号
                }
            }
        }
    }

    // =================== setter / getter ======================

    public void setOtterLoaderFactory(OtterLoaderFactory otterLoaderFactory) {
        this.otterLoaderFactory = otterLoaderFactory;
    }

    public void setDbLoadInterceptor(LoadInterceptor dbLoadInterceptor) {
        this.dbLoadInterceptor = dbLoadInterceptor;
    }

}
