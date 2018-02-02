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

package com.alibaba.otter.shared.arbitrate.impl.setl.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.util.CollectionUtils;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateLifeCycle;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.ReplyProcessQueue;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StageProgress;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.common.collect.OtterMigrateMap;

/**
 * @author jianghang 2012-9-27 下午10:12:35
 * @version 4.1.0
 */
public class MemoryStageController extends ArbitrateLifeCycle {

    private AtomicLong                        atomicMaxProcessId = new AtomicLong(0);
    private Map<StageType, ReplyProcessQueue> replys;
    private Map<Long, StageProgress>          progress;
    private BlockingQueue<TerminEventData>    termins;
    private StageProgress                     nullProgress       = new StageProgress();

    public MemoryStageController(Long pipelineId){
        super(pipelineId);

        replys = OtterMigrateMap.makeComputingMap(new Function<StageType, ReplyProcessQueue>() {

            public ReplyProcessQueue apply(StageType input) {
                int size = ArbitrateConfigUtils.getParallelism(getPipelineId()) * 10;
                if (size < 100) {
                    size = 100;
                }
                return new ReplyProcessQueue(size);
            }
        });

        progress = new MapMaker().makeMap();
        termins = new LinkedBlockingQueue<TerminEventData>(20);
    }

    public Long waitForProcess(StageType stage) throws InterruptedException {
        if (stage.isSelect() && !replys.containsKey(stage)) {
            initSelect();
        }

        Long processId = replys.get(stage).take();
        if (stage.isSelect()) {// select一旦分出processId，就需要在progress中记录一笔，用于判断谁是最小的一个processId
            progress.put(processId, nullProgress);
        }

        return processId;
    }

    public EtlEventData getLastData(Long processId) {
        return progress.get(processId).getData();
    }

    public synchronized void destory() {
        replys.clear();
        progress.clear();
    }

    public synchronized void clearProgress(Long processId) {
        progress.remove(processId);
    }

    /**
     * 处理异常termin结束
     */
    public synchronized void termin(TerminType type) {
        // 构建termin信号
        List<Long> processIds = new ArrayList<Long>(progress.keySet());
        Collections.sort(processIds);// 做一下排序
        for (Long processId : processIds) {
            EtlEventData eventData = progress.get(processId).getData();

            TerminEventData data = new TerminEventData();
            data.setPipelineId(getPipelineId());
            data.setType(type);
            data.setCode("channel");
            data.setDesc(type.toString());
            data.setProcessId(processId);
            if (eventData != null) {
                data.setBatchId(eventData.getBatchId());
                data.setCurrNid(eventData.getCurrNid());
                data.setStartTime(eventData.getStartTime());
                data.setEndTime(eventData.getEndTime());
                data.setFirstTime(eventData.getFirstTime());
                data.setNumber(eventData.getNumber());
                data.setSize(eventData.getSize());
                data.setExts(eventData.getExts());
            }
            offerTermin(data);
            progress.remove(processId);
        }

        // 重新初始化一下select调度
        initSelect();
    }

    public synchronized boolean single(StageType stage, EtlEventData etlEventData) {
        boolean result = false;
        switch (stage) {
            case SELECT:
                if (progress.containsKey(etlEventData.getProcessId())) {// 可能发生了rollback，对应的progress已经被废弃
                    progress.put(etlEventData.getProcessId(), new StageProgress(stage, etlEventData));
                    replys.get(StageType.EXTRACT).offer(etlEventData.getProcessId());
                    result = true;
                }
                break;
            case EXTRACT:
                if (progress.containsKey(etlEventData.getProcessId())) {
                    progress.put(etlEventData.getProcessId(), new StageProgress(stage, etlEventData));
                    replys.get(StageType.TRANSFORM).offer(etlEventData.getProcessId());
                    result = true;
                }
                break;
            case TRANSFORM:
                if (progress.containsKey(etlEventData.getProcessId())) {
                    progress.put(etlEventData.getProcessId(), new StageProgress(stage, etlEventData));
                    result = true;
                }
                // 并不是立即触发，通知最小的一个process启动
                computeNextLoad();
                break;
            case LOAD:
                Object removed = progress.remove(etlEventData.getProcessId());
                // 并不是立即触发，通知下一个最小的一个process启动
                computeNextLoad();
                // 一个process完成了，自动添加下一个process
                if (removed != null) {
                    replys.get(StageType.SELECT).offer(atomicMaxProcessId.incrementAndGet());
                    result = true;
                }
                break;
            default:
                break;
        }

        return result;
    }

    public void offerTermin(TerminEventData data) {
        try {
            termins.put(data);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public void ackTermin(TerminEventData data) {
        // do nothing
    }

    public int sizeTermin() {
        return termins.size();
    }

    public TerminEventData waitTermin() throws InterruptedException {
        return termins.take();
    }

    private synchronized void initSelect() {
        // 第一次/出现ROLLBACK/RESTART事件，删除了所有调度信号后，重新初始化一下select
        // stage的数据，初始大小为并行度大小
        // 后续的select的reply队列变化，由load single时直接添加
        ReplyProcessQueue queue = replys.get(StageType.SELECT);
        int parallelism = ArbitrateConfigUtils.getParallelism(getPipelineId());
        while (parallelism-- > 0 && queue.size() <= parallelism) {
            queue.offer(atomicMaxProcessId.incrementAndGet());
        }
    }

    /**
     * 计算下一个load的processId
     */
    private void computeNextLoad() {
        Long processId = getMinTransformedProcessId();
        if (processId != null) {
            replys.get(StageType.LOAD).offer(processId);
        }
    }

    /**
     * 获取最小一个符合条件的processId
     */
    private synchronized Long getMinTransformedProcessId() {
        if (!CollectionUtils.isEmpty(progress)) {
            Long processId = Collections.min(progress.keySet());
            StageProgress stage = progress.get(processId);
            // stage可能为空，针对select未完成时，对应的值就为null
            if (stage != null && stage != nullProgress && stage.getStage().isTransform()) {
                return processId;
            }
        }

        return null;
    }

}
