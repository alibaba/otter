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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.ExtractArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.lb.LoadBalanceFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 基于rpc方式实现的extract调度
 * 
 * @author jianghang 2012-9-29 上午10:59:24
 * @version 4.1.0
 */
public class ExtractRpcArbitrateEvent implements ExtractArbitrateEvent {

    private static final Logger     logger    = LoggerFactory.getLogger(ExtractRpcArbitrateEvent.class);
    private ZkClientx               zookeeper = ZooKeeperClient.getInstance();
    private RpcStageEventDispatcher rpcStageEventDispatcher;

    public EtlEventData await(Long pipelineId) throws InterruptedException {
        Assert.notNull(pipelineId);

        PermitMonitor permitMonitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        permitMonitor.waitForPermit();// 阻塞等待授权

        RpcStageController stageController = ArbitrateFactory.getInstance(pipelineId, RpcStageController.class);
        Long processId = stageController.waitForProcess(StageType.EXTRACT); // 符合条件的processId

        ChannelStatus status = permitMonitor.getChannelPermit();
        if (status.isStart() || status.isPause()) {// pause状态也让其处理，避免误删除pause状态的processId，导致通道挂起
            EtlEventData eventData = stageController.getLastData(processId);
            Node node = LoadBalanceFactory.getNextTransformNode(pipelineId);// 获取下一个处理节点信息
            if (node == null) {// 没有后端节点
                throw new ArbitrateException("Extract_single", "no next node");
            } else {
                eventData.setNextNid(node.getId());
                return eventData;// 只有这一条路返回
            }
        } else {
            logger.warn("pipelineId[{}] extract ignore processId[{}] by status[{}]", new Object[] { pipelineId,
                    processId, status });
            String path = StagePathUtils.getProcess(pipelineId, processId);
            zookeeper.exists(path);

            return await(pipelineId);// 递归调用
        }
    }

    public void single(EtlEventData data) {
        Assert.notNull(data);
        rpcStageEventDispatcher.single(StageType.EXTRACT, data);// 通知下一个节点
    }

    public void setRpcStageEventDispatcher(RpcStageEventDispatcher rpcStageEventDispatcher) {
        this.rpcStageEventDispatcher = rpcStageEventDispatcher;
    }

}
