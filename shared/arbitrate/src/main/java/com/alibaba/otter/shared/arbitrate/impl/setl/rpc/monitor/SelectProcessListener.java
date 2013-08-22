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

package com.alibaba.otter.shared.arbitrate.impl.setl.rpc.monitor;

import java.util.List;

import org.I0Itec.zkclient.exception.ZkException;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.MainstemMonitor;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.listener.MainstemListener;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.listener.PermitListener;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.arbitrate.model.ProcessNodeEventData;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter.ArbitrateMode;
import com.alibaba.otter.shared.common.utils.JsonUtils;

/**
 * 监听process的变化，发现节点数<并行度，则添加一个可调度的process
 * 
 * @author jianghang 2012-9-28 下午10:00:22
 * @version 4.1.0
 */
public class SelectProcessListener extends AbstractProcessListener implements ProcessListener, PermitListener, MainstemListener {

    private volatile boolean isPermit = true;
    private PermitMonitor    permitMonitor;
    private MainstemMonitor  mainstemMonitor;

    public SelectProcessListener(Long pipelineId){
        super(pipelineId);
        permitMonitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        mainstemMonitor = ArbitrateFactory.getInstance(pipelineId, MainstemMonitor.class);
        permitMonitor.addListener(this);
        mainstemMonitor.addListener(this);

        recovery(getPipelineId());// 启动时载入一次
    }

    public void processChanged(List<Long> processIds) {
        super.processChanged(processIds);
        // add by ljh at 2012-09-13,解决zookeeper ConnectionLoss问题
        for (Long processId : processIds) {
            if (!replyProcessIds.contains(processId)) {
                logger.warn("process is not in order, please check processId:{}", processId);
                addReply(processId);
            }
        }

        try {
            String path = StagePathUtils.getProcessRoot(getPipelineId());
            // 根据并行度创建任务
            int size = ArbitrateConfigUtils.getParallelism(getPipelineId()) - processIds.size();
            if (size > 0) {// 创建一个节点
                PermitMonitor permit = ArbitrateFactory.getInstance(getPipelineId(), PermitMonitor.class);
                if (permit.isPermit() == false) { // 如果非授权，则不做任何处理
                    return;
                }

                String mainStemPath = StagePathUtils.getMainStem(getPipelineId());
                byte[] bytes = zookeeper.readData(mainStemPath, true);
                if (bytes == null) {
                    return;
                }

                MainStemEventData eventData = JsonUtils.unmarshalFromByte(bytes, MainStemEventData.class);
                if (eventData.getNid().equals(ArbitrateConfigUtils.getCurrentNid()) == false) {
                    return;// 如果非自己设置的mainStem,则不做任何处理
                }

                // 目前select只会在一个节点上部署，只需要单机版锁即可，后续可采用分布式锁进行并发控制
                // DistributedLock lock = new DistributedLock(PathUtils.getSelectLock(getPipelineId()));
                // try {
                // lock.lock();
                // //创建process
                // } finally {
                // lock.unlock();
                // }

                synchronized (this) {
                    // 重新再取一次, dobble-check
                    List<String> currentProcesses = zookeeper.getChildren(path);
                    size = ArbitrateConfigUtils.getParallelism(getPipelineId()) - currentProcesses.size();
                    if (size > 0) {// 创建一个节点
                        ProcessNodeEventData nodeData = new ProcessNodeEventData();
                        nodeData.setStatus(ProcessNodeEventData.Status.UNUSED);// 标记为未使用
                        nodeData.setMode(ArbitrateMode.RPC);
                        nodeData.setNid(ArbitrateConfigUtils.getCurrentNid());
                        byte[] nodeBytes = JsonUtils.marshalToByte(nodeData);
                        String processPath = zookeeper.create(path + "/", nodeBytes, CreateMode.PERSISTENT_SEQUENTIAL);
                        // 创建为顺序的节点
                        String processNode = StringUtils.substringAfterLast(processPath, "/");
                        Long processId = StagePathUtils.getProcessId(processNode);// 添加到当前的process列表
                        addReply(processId);
                    }
                }

            }
        } catch (ZkException e) {
            recovery(getPipelineId());// 出现异常后进行一次recovery，读取一下当前最新值，解决出现ConnectionLoss时create成功问题
            logger.error("add process error!", e);
        }

    }

    public void processChanged(boolean isPermit) {
        if (this.isPermit != isPermit && isPermit == true) { // isPemit从未授权到一个授权的变动
            processMonitor.reload(); // 触发一下processChanged，快速的创建process
        }

        this.isPermit = isPermit;
    }

    /**
     * 尝试载入一下上一次未使用的processId，可能发生mainstem切换，新的S模块需要感知前S模块已创建但未使用的process，不然就是一个死锁。而针对已经使用的processId会由e/t/l节点进行处理
     */
    private void recovery(Long pipelineId) {
        List<Long> currentProcessIds = processMonitor.getCurrentProcessIds(false);
        for (Long processId : currentProcessIds) {
            String path = StagePathUtils.getProcess(pipelineId, processId);
            try {
                byte[] bytes = zookeeper.readData(path);
                ProcessNodeEventData nodeData = JsonUtils.unmarshalFromByte(bytes, ProcessNodeEventData.class);
                if (nodeData.getStatus().isUnUsed()) {// 加入未使用的processId
                    addReply(processId);
                }
            } catch (ZkException e) {
                logger.error("recovery error!", e);
            }
        }
    }

    public void processActiveEnter() {
        recovery(getPipelineId());
        processMonitor.reload(); // 触发一下processChanged
    }

    public void processActiveExit() {
        ArbitrateFactory.destory(getPipelineId(), this.getClass());
    }

    public void destory() {
        // 取消注册
        permitMonitor.removeListener(this);
        mainstemMonitor.removeListener(this);
        super.destory();
    }
}
