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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.LoadArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 基于rpc方式实现的load调度
 * 
 * @author jianghang 2012-9-29 上午10:59:24
 * @version 4.1.0
 */
public class LoadRpcArbitrateEvent implements LoadArbitrateEvent {

    private static final Logger     logger    = LoggerFactory.getLogger(LoadRpcArbitrateEvent.class);
    private TerminRpcArbitrateEvent terminEvent;
    private ZkClientx               zookeeper = ZooKeeperClient.getInstance();
    private RpcStageEventDispatcher rpcStageEventDispatcher;

    public EtlEventData await(Long pipelineId) throws InterruptedException {
        Assert.notNull(pipelineId);

        PermitMonitor permitMonitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        permitMonitor.waitForPermit();// 阻塞等待授权

        RpcStageController stageController = ArbitrateFactory.getInstance(pipelineId, RpcStageController.class);
        Long processId = stageController.waitForProcess(StageType.LOAD); // 符合条件的processId

        ChannelStatus status = permitMonitor.getChannelPermit();
        if (status.isStart()) {// 即时查询一下当前的状态，状态随时可能会变
            return stageController.getLastData(processId);
        } else {
            // 需要进一步check，避免丢失load信号
            status = permitMonitor.getChannelPermit(true);
            if (status.isStart()) {
                return stageController.getLastData(processId);
            } else if (status.isPause()) {
                String path = StagePathUtils.getProcess(pipelineId, processId);
                if (zookeeper.exists(path)) { // 如果存在process，那说明没有被rollback掉(可能刚好在做rollback)，这种运行进行load处理
                    return stageController.getLastData(processId);
                }
            }

            logger.warn("pipelineId[{}] load ignore processId[{}] by status[{}]", new Object[] { pipelineId, processId,
                    status });
            return await(pipelineId);// 递归调用
        }
    }

    public void single(final EtlEventData data) {
        Assert.notNull(data);
        data.setEndTime(new Date().getTime());// 返回当前时间
        boolean result = rpcStageEventDispatcher.single(StageType.LOAD, data);// 通知下一个节点，下一个节点也肯定会是自己

        if (result) {
            // 直接异步处理termin，更快速的返回, modify by ljh at 2013-02-25
            // 减少Load await/single所占用的时间，尽快返回，因为两个load之间的传递可以尽可能不走zookeeper完成
            TerminExecutor executor = ArbitrateFactory.getInstance(data.getPipelineId(), TerminExecutor.class);
            executor.submit(new Runnable() {

                public void run() {
                    // 调用Termin信号
                    TerminEventData termin = new TerminEventData();
                    termin.setPipelineId(data.getPipelineId());
                    termin.setProcessId(data.getProcessId());
                    termin.setStartTime(data.getStartTime());
                    termin.setEndTime(data.getEndTime());
                    termin.setFirstTime(data.getFirstTime());
                    termin.setNumber(data.getNumber());
                    termin.setBatchId(data.getBatchId());
                    termin.setSize(data.getSize());
                    termin.setExts(data.getExts());
                    termin.setType(TerminType.NORMAL);
                    termin.setCode("setl");
                    termin.setDesc("");
                    termin.setCurrNid(ArbitrateConfigUtils.getCurrentNid());
                    terminEvent.single(termin);
                }
            });
        }
    }

    public void setRpcStageEventDispatcher(RpcStageEventDispatcher rpcStageEventDispatcher) {
        this.rpcStageEventDispatcher = rpcStageEventDispatcher;
    }

    public void setTerminEvent(TerminRpcArbitrateEvent terminEvent) {
        this.terminEvent = terminEvent;
    }

}
