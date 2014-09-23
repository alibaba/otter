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

package com.alibaba.otter.node.etl.extract;

import java.util.List;

import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.common.jmx.StageAggregation.AggregationItem;
import com.alibaba.otter.node.etl.common.pipe.PipeKey;
import com.alibaba.otter.node.etl.common.task.GlobalTask;
import com.alibaba.otter.node.etl.conflict.FileBatchConflictDetectService;
import com.alibaba.otter.node.etl.extract.extractor.OtterExtractorFactory;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.FileBatch;

/**
 * extract工作线程,负责桥接连接仲裁器
 * 
 * @author xiaoqing.zhouxq
 */
public class ExtractTask extends GlobalTask {

    private OtterExtractorFactory          otterExtractorFactory;
    private FileBatchConflictDetectService fileBatchConflictDetectService;

    public ExtractTask(Long pipelineId){
        super(pipelineId);
    }

    public void run() {
        MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipelineId));
        while (running) {
            try {
                final EtlEventData etlEventData = arbitrateEventService.extractEvent().await(pipelineId);
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
                        Thread.currentThread().setName(createTaskName(pipelineId, "ExtractWorker"));
                        try {
                            pipeline = configClientService.findPipeline(pipelineId);
                            List<PipeKey> keys = (List<PipeKey>) etlEventData.getDesc();
                            long nextNodeId = etlEventData.getNextNid();
                            DbBatch dbBatch = rowDataPipeDelegate.get(keys);

                            // 可能拿到为null，因为内存不足或者网络异常，长时间阻塞时，导致从pipe拿数据出现异常，数据可能被上一个节点已经删除
                            if (dbBatch == null) {
                                processMissData(pipelineId, "extract miss data with keys:" + keys.toString());
                                return;
                            }

                            otterExtractorFactory.extract(dbBatch);// 重新装配一下数据
                            if (dbBatch.getFileBatch() != null
                                && !CollectionUtils.isEmpty(dbBatch.getFileBatch().getFiles())
                                && pipeline.getParameters().getFileDetect()) { // 判断一下是否有文件同步，并且需要进行文件对比
                                // 对比一下中美图片是否有变化
                                FileBatch fileBatch = fileBatchConflictDetectService.detect(dbBatch.getFileBatch(),
                                                                                            nextNodeId);
                                dbBatch.setFileBatch(fileBatch);
                            }

                            List<PipeKey> pipeKeys = rowDataPipeDelegate.put(dbBatch, nextNodeId);
                            etlEventData.setDesc(pipeKeys);

                            if (profiling) {
                                Long profilingEndTime = System.currentTimeMillis();
                                stageAggregationCollector.push(pipelineId,
                                                               StageType.EXTRACT,
                                                               new AggregationItem(profilingStartTime, profilingEndTime));
                            }
                            arbitrateEventService.extractEvent().single(etlEventData);
                        } catch (Throwable e) {
                            if (!isInterrupt(e)) {
                                logger.error(String.format("[%d] extractwork executor is error! data:%s", pipelineId,
                                                           etlEventData), e);
                                sendRollbackTermin(pipelineId, e);
                            } else {
                                logger.info(String.format("[%d] extractwork executor is interrrupt! data:%s",
                                                          pipelineId, etlEventData), e);
                            }
                        } finally {
                            Thread.currentThread().setName(currentName);
                            MDC.remove(OtterConstants.splitPipelineLogFileKey);
                        }
                    }
                };

                // 构造pending任务，可在关闭线程时退出任务
                SetlFuture extractFuture = new SetlFuture(StageType.EXTRACT, etlEventData.getProcessId(),
                                                          pendingFuture, task);
                executorService.execute(extractFuture);
            } catch (Throwable e) {
                if (isInterrupt(e)) {
                    logger.info(String.format("[%s] extractTask is interrupted!", pipelineId), e);
                    return;
                } else {
                    logger.error(String.format("[%s] extractTask is error!", pipelineId), e);
                    sendRollbackTermin(pipelineId, e);
                }
            }
        }
    }

    // =================== setter / getter ======================

    public void setOtterExtractorFactory(OtterExtractorFactory otterExtractorFactory) {
        this.otterExtractorFactory = otterExtractorFactory;
    }

    public void setFileBatchConflictDetectService(FileBatchConflictDetectService fileBatchConflictDetectService) {
        this.fileBatchConflictDetectService = fileBatchConflictDetectService;
    }

}
