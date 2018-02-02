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

package com.alibaba.otter.shared.arbitrate.impl.setl.rpc;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateLifeCycle;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.ReplyProcessQueue;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StageProgress;
import com.alibaba.otter.shared.arbitrate.impl.setl.rpc.monitor.ProcessListener;
import com.alibaba.otter.shared.arbitrate.impl.setl.rpc.monitor.ProcessMonitor;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.common.collect.OtterMigrateMap;

/**
 * 基于rpc的stage调度控制器，排除select调度,主要控制e/t/l的调度控制
 * 
 * @author jianghang 2012-9-28 下午10:05:26
 * @version 4.1.0
 */
public class RpcStageController extends ArbitrateLifeCycle implements ProcessListener {

    private static final Logger               logger                 = LoggerFactory.getLogger(RpcStageController.class);
    private Map<StageType, ReplyProcessQueue> replys;
    private Map<Long, StageProgress>          progress;
    private ProcessMonitor                    processMonitor;
    private volatile Long                     lastestLoadedProcessId = -1L;                                              // 最近一次同步成功的processId

    public RpcStageController(Long pipelineId){
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
        // 注册一下监听事件变化
        processMonitor = ArbitrateFactory.getInstance(pipelineId, ProcessMonitor.class);
        processMonitor.addListener(this);
        processMonitor.reload();
    }

    public Long waitForProcess(StageType stage) throws InterruptedException {
        if (stage.isSelect()) {
            throw new ArbitrateException("not support");
        }

        return replys.get(stage).take();
    }

    /**
     * 获取上一个stage传递的数据信息
     */
    public EtlEventData getLastData(Long processId) {
        return progress.get(processId).getData();
    }

    public void destory() {
        processMonitor.removeListener(this);
        replys.clear();
        progress.clear();
    }

    public synchronized boolean single(StageType stage, EtlEventData etlEventData) {
        boolean result = true;
        switch (stage) {
            case SELECT:
                progress.put(etlEventData.getProcessId(), new StageProgress(StageType.SELECT, etlEventData));
                replys.get(StageType.EXTRACT).offer(etlEventData.getProcessId());
                break;
            case EXTRACT:
                progress.put(etlEventData.getProcessId(), new StageProgress(StageType.EXTRACT, etlEventData));
                replys.get(StageType.TRANSFORM).offer(etlEventData.getProcessId());
                break;
            case TRANSFORM:
                progress.put(etlEventData.getProcessId(), new StageProgress(StageType.TRANSFORM, etlEventData));
                // 并不是立即触发，通知最小的一个process启动
                computeNextLoad();
                break;
            case LOAD:
                Object removed = progress.remove(etlEventData.getProcessId());
                // 并不是立即触发，通知下一个最小的一个process启动
                if (removed == null) {
                    result = false;
                } else {
                    // 如果2个process 1和2, 1先执行完了load,此时2还不符合条件，等2到了Transform时，还需要依赖zookeeper的process列表变化进行判断
                    // 记录一下上一次同步成功的processId，提升load响应速度，方便在内存中直接判断
                    lastestLoadedProcessId = etlEventData.getProcessId();
                    computeNextLoad(); // 通知load processId，触发下一个
                }
                break;
            default:
                break;
        }

        return result;
    }

    /**
     * 计算下一个load的processId
     */
    private void computeNextLoad() {
        // 针对上一个id为本地load成功的，直接忽略，触发下一个id
        Long processId = getMinTransformedProcessId(lastestLoadedProcessId);
        if (processId != null) {
            replys.get(StageType.LOAD).offer(processId);
        }
    }

    /**
     * 获取最小一个符合条件的processId，排除loadedProcessId
     */
    private Long getMinTransformedProcessId(Long loadedProcessId) {
        ProcessMonitor processMonitor = ArbitrateFactory.getInstance(getPipelineId(), ProcessMonitor.class);
        List<Long> processIds = processMonitor.getCurrentProcessIds();
        // 如果需要当前node处理当前process的load时，rpc请求一定会将对应的stage状态发到这机器上，并保存到progress中
        if (!CollectionUtils.isEmpty(processIds) && !CollectionUtils.isEmpty(progress)) {
            // 上一次load成功的在当前的processId中不存在，可能有两种情况:
            // 1. zk还未将数据通知过来，当前current processIds还是为老版本的值
            // 2. processId已经被删除，比如好久没有数据同步了，定时触发时发觉列表一直为空
            // if (loadedProcessId != null && !processIds.contains(loadedProcessId)) {
            // // 强制刷新一次，不过也可能是刷到老版本的值，杭州leader还没同步到美国
            // processIds = processMonitor.getCurrentProcessIds(true);
            // }

            Long result = null;
            // 做的一个优化，如果上一个processId load成功是在本机，直接忽略
            // 因为存在一个问题：比如两个process，都先完成了T模块，然后逐个触发L模块，此时第二个process需要等zookeeper回调watcher时才会被触发
            for (Long processId : processIds) {
                if (loadedProcessId == null || processId > loadedProcessId) {
                    result = processId;
                    break;
                }
            }

            // 如果不存在符合>loadedProcessId的记录，直接假设下一个processId就是上一个id+1
            // 因为processId目前的机制永远只会递增
            if (result == null) {
                result = loadedProcessId + 1;
            }

            if (result != null) {
                StageProgress stage = progress.get(result);
                if (stage != null && stage.getStage().isTransform()) {
                    return result;
                } else {
                    logger.info("rpc compute [{}] but stage [{}]", result, stage == null ? null : stage.getStage());
                    return null;
                }
            }
        }

        return null;
    }

    public void processChanged(List<Long> processIds) {
        compareProgress(processIds);

        for (ReplyProcessQueue replyProcessIds : replys.values()) {
            compareReply(processIds, replyProcessIds);
        }

        // process发生变化，可能是process load完成，需要触发下一个process进行load
        computeNextLoad();
    }

    /**
     * 删除已经被废弃的processId
     */
    private synchronized void compareProgress(List<Long> processIds) {
        if (CollectionUtils.isEmpty(processIds) == false) {
            Long minProcessId = processIds.get(0);
            // 对比一下progress中的记录，如果小于当前最小的processId，直接删除内存中的记录
            // 因为发生跨机器调用或者出现restart指令，对应的process记录不会被删除
            for (Long processId : progress.keySet()) {
                if (processId < minProcessId) {
                    progress.remove(processId);
                }
            }
        }
    }

    /**
     * 将当前的符合条件的processIds和当前的reply queue进行校对，剔除不在processIds里的内容
     */
    private synchronized void compareReply(List<Long> processIds, ReplyProcessQueue replyProcessIds) {
        Object[] replyIds = replyProcessIds.toArray();
        for (Object replyId : replyIds) {
            if (processIds.contains((Long) replyId) == false) { // 判断reply id是否在当前processId列表中
                // 因为存在并发问题，如在执行Listener事件的同时，可能触发了process的创建，这时新建的processId会进入到reply队列中
                // 此时接受到的processIds变量为上一个版本的内容，所以会删除新建的process，导致整个通道被挂住
                if (CollectionUtils.isEmpty(processIds) == false) {
                    Long processId = processIds.get(0);
                    if (processId > (Long) replyId) { // 如果当前最小的processId都大于replyId, processId都是递增创建的
                        logger.info("## {} remove reply id [{}]", ClassUtils.getShortClassName(this.getClass()),
                                    (Long) replyId);
                        replyProcessIds.remove((Long) replyId);
                        progress.remove((Long) replyId);
                    }
                }
            }
        }
    }

}
