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

package com.alibaba.otter.node.etl.transform;

import java.util.List;
import java.util.Map;

import org.slf4j.MDC;

import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.common.jmx.StageAggregation.AggregationItem;
import com.alibaba.otter.node.etl.common.pipe.PipeKey;
import com.alibaba.otter.node.etl.common.task.GlobalTask;
import com.alibaba.otter.node.etl.extract.SetlFuture;
import com.alibaba.otter.node.etl.transform.transformer.OtterTransformerFactory;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.etl.model.BatchObject;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.RowBatch;

/**
 * transform工作线程,负责桥接连接仲裁器,Config,translate
 * 
 * @author jianghang 2011-10-11 下午04:14:11
 * @version 4.0.0
 */
public class TransformTask extends GlobalTask {

    private OtterTransformerFactory otterTransformerFactory;

    public TransformTask(Long pipelineId){
        super(pipelineId);
    }

    public void run() {
        MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipelineId));
        while (running) {
            try {
                final EtlEventData etlEventData = arbitrateEventService.transformEvent().await(pipelineId);
                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        // 设置profiling信息
                        boolean profiling = isProfiling();
                        Long profilingStartTime = null;
                        if (profiling) {
                            profilingStartTime = System.currentTimeMillis();
                        }

                        MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipelineId));
                        String currentName = Thread.currentThread().getName();
                        Thread.currentThread().setName(createTaskName(pipelineId, "transformWorker"));

                        try {
                            // 后续可判断同步数据是否为rowData
                            List<PipeKey> keys = (List<PipeKey>) etlEventData.getDesc();
                            DbBatch dbBatch = rowDataPipeDelegate.get(keys);

                            // 可能拿到为null，因为内存不足或者网络异常，长时间阻塞时，导致从pipe拿数据出现异常，数据可能被上一个节点已经删除
                            if (dbBatch == null) {
                                processMissData(pipelineId, "transform miss data with keys:" + keys.toString());
                                return;
                            }

                            // 根据对应的tid，转化为目标端的tid。后续可进行字段的加工处理
                            // 暂时认为rowBatchs和fileBatchs不会有异构数据的转化
                            Map<Class, BatchObject> dataBatchs = otterTransformerFactory.transform(dbBatch.getRowBatch());

                            // 可能存在同一个Pipeline下有Mq和Db两种同步类型
                            dbBatch.setRowBatch((RowBatch) dataBatchs.get(EventData.class));

                            if (dbBatch.getFileBatch() != null) {
                                Map<Class, BatchObject> fileBatchs = otterTransformerFactory.transform(dbBatch.getFileBatch());
                                dbBatch.setFileBatch((FileBatch) fileBatchs.get(FileData.class));
                            }
                            // 传递给下一个流程
                            List<PipeKey> nextKeys = rowDataPipeDelegate.put(dbBatch, etlEventData.getNextNid());
                            etlEventData.setDesc(nextKeys);

                            if (profiling) {
                                Long profilingEndTime = System.currentTimeMillis();
                                stageAggregationCollector.push(pipelineId,
                                                               StageType.TRANSFORM,
                                                               new AggregationItem(profilingStartTime, profilingEndTime));
                            }
                            // 处理完成后通知single已完成
                            arbitrateEventService.transformEvent().single(etlEventData);
                        } catch (Throwable e) {
                            if (!isInterrupt(e)) {
                                logger.error(String.format("[%s] transformWork executor is error! data:%s", pipelineId,
                                                           etlEventData), e);
                                sendRollbackTermin(pipelineId, e);
                            } else {
                                logger.info(String.format("[%s] transformWork executor is interrrupt! data:%s",
                                                          pipelineId, etlEventData), e);
                            }
                        } finally {
                            Thread.currentThread().setName(currentName);
                            MDC.remove(OtterConstants.splitPipelineLogFileKey);
                        }
                    }
                };

                // 构造pending任务，可在关闭线程时退出任务
                SetlFuture extractFuture = new SetlFuture(StageType.TRANSFORM, etlEventData.getProcessId(),
                                                          pendingFuture, task);
                executorService.execute(extractFuture);

            } catch (Throwable e) {
                if (isInterrupt(e)) {
                    logger.info(String.format("[%s] transformTask is interrupted!", pipelineId), e);
                    return;
                } else {
                    logger.error(String.format("[%s] transformTask is error!", pipelineId), e);
                    sendRollbackTermin(pipelineId, e);
                }
            }
        }
    }

    // =================== setter / getter ======================

    public void setOtterTransformerFactory(OtterTransformerFactory otterTransformerFactory) {
        this.otterTransformerFactory = otterTransformerFactory;
    }
}
