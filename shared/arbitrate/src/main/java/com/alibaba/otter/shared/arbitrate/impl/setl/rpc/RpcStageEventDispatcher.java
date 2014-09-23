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

import org.springframework.util.Assert;

import com.alibaba.otter.shared.arbitrate.impl.communication.ArbitrateCommmunicationClient;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.communication.core.CommunicationRegistry;
import com.alibaba.otter.shared.communication.model.arbitrate.ArbitrateEventType;
import com.alibaba.otter.shared.communication.model.arbitrate.StageSingleEvent;

/**
 * 分发rpc的请求，根据不同的pipelineId分发到不同的{@link RpcStageController}实例上去
 * 
 * @author jianghang 2012-9-29 上午10:26:38
 * @version 4.1.0
 */
public class RpcStageEventDispatcher {

    private ArbitrateCommmunicationClient arbitrateCommmunicationClient;

    public RpcStageEventDispatcher(){
        CommunicationRegistry.regist(ArbitrateEventType.stageSingle, this);
    }

    /**
     * 接收rpc请求的调用
     */
    protected boolean onStageSingle(StageSingleEvent event) {
        Assert.notNull(event.getPipelineId());
        // 根据pipeline找到对应的实例
        RpcStageController controller = ArbitrateFactory.getInstance(event.getPipelineId(), RpcStageController.class);
        return controller.single(event.getStage(), (EtlEventData) event.getData());
    }

    /**
     * 触发通知
     */
    public boolean single(StageType stage, EtlEventData eventData) {
        Assert.notNull(eventData);
        eventData.setCurrNid(ArbitrateConfigUtils.getCurrentNid());

        StageSingleEvent event = new StageSingleEvent(ArbitrateEventType.stageSingle);
        event.setPipelineId(eventData.getPipelineId());
        event.setStage(stage);
        event.setData(eventData);

        if (isLocal(eventData.getNextNid())) {// 判断是否为本地jvm
            return onStageSingle(event);
        } else {
            return (Boolean) arbitrateCommmunicationClient.call(eventData.getNextNid(), event);// rpc通知下一个节点
        }
    }

    private boolean isLocal(Long targetNodeId) {
        return ArbitrateConfigUtils.getCurrentNid().equals(targetNodeId);
    }

    public void setArbitrateCommmunicationClient(ArbitrateCommmunicationClient arbitrateCommmunicationClient) {
        this.arbitrateCommmunicationClient = arbitrateCommmunicationClient;
    }

}
