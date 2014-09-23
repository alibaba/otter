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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.communication.ArbitrateCommmunicationClient;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.TerminArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.termin.WarningTerminProcess;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.communication.model.arbitrate.StopChannelEvent;

/**
 * 基于内存版本的termin信号处理
 * 
 * @author jianghang 2012-9-27 下午11:30:13
 * @version 4.1.0
 */
public class TerminMemoryArbitrateEvent implements TerminArbitrateEvent {

    private static final Logger           logger = LoggerFactory.getLogger(TerminMemoryArbitrateEvent.class);
    private ArbitrateCommmunicationClient arbitrateCommmunicationClient;
    private WarningTerminProcess          warningTerminProcess;
    private ChannelArbitrateEvent         channelEvent;

    public TerminEventData await(Long pipelineId) throws InterruptedException {
        Assert.notNull(pipelineId);
        MemoryStageController stageController = ArbitrateFactory.getInstance(pipelineId, MemoryStageController.class);
        TerminEventData eventData = stageController.waitTermin();
        if (logger.isDebugEnabled()) {
            logger.debug("## await pipeline[{}] processId[{}] is termin", pipelineId, eventData.getProcessId());
        }

        return eventData;
    }

    public void exhaust(Long pipelineId) {
        Assert.notNull(pipelineId);
        MemoryStageController stageController = ArbitrateFactory.getInstance(pipelineId, MemoryStageController.class);
        int size = stageController.sizeTermin();
        try {
            for (int i = 0; i < size; i++) {
                TerminEventData data = stageController.waitTermin();
                ack(data);
            }
        } catch (InterruptedException e) {
            throw new ArbitrateException(e);
        }
    }

    public void single(TerminEventData data) {
        // 正向处理
        final TerminType type = data.getType();
        MemoryStageController stageController = ArbitrateFactory.getInstance(data.getPipelineId(),
                                                                             MemoryStageController.class);
        if (type.isNormal()) {
            Assert.notNull(data.getProcessId());
            stageController.offerTermin(data);
        } else if (type.isWarning()) {
            warningTerminProcess.process(data); // warn单独处理，不需要关闭相关的pipeline
        } else {
            // 内存版可以简化处理rollback/restart/shutdown模型，不需要进行process的termin操作处理
            Channel channel = ArbitrateConfigUtils.getChannel(data.getPipelineId());
            if (data.getType().isRollback()) {
                boolean paused = channelEvent.pause(channel.getId(), false);
                if (paused) {// 如果pause成功，则发送报警信息
                    warningTerminProcess.process(data);
                }
            } else if (data.getType().isShutdown()) {
                boolean shutdowned = channelEvent.stop(channel.getId(), false);
                // 发送报警信息
                if (shutdowned) {
                    warningTerminProcess.process(data);
                }
                // 发送关闭命令给manager
                StopChannelEvent event = new StopChannelEvent();
                event.setChannelId(channel.getId());
                arbitrateCommmunicationClient.callManager(event);
            } else if (data.getType().isRestart()) {
                boolean restarted = channelEvent.restart(channel.getId(), false);
                // 发送报警信息
                if (restarted) {
                    warningTerminProcess.process(data);
                }
            }

            stageController.termin(data.getType());// 内存中构造异常termin信号返回
        }

    }

    public void ack(TerminEventData data) {
        MemoryStageController stageController = ArbitrateFactory.getInstance(data.getPipelineId(),
                                                                             MemoryStageController.class);
        stageController.ackTermin(data);
    }

    public int size(Long pipelineId) {
        MemoryStageController stageController = ArbitrateFactory.getInstance(pipelineId, MemoryStageController.class);
        return stageController.sizeTermin();
    }

    // ================== setter / getter ===================
    public void setArbitrateCommmunicationClient(ArbitrateCommmunicationClient arbitrateCommmunicationClient) {
        this.arbitrateCommmunicationClient = arbitrateCommmunicationClient;
    }

    public void setWarningTerminProcess(WarningTerminProcess warningTerminProcess) {
        this.warningTerminProcess = warningTerminProcess;
    }

    public void setChannelEvent(ChannelArbitrateEvent channelEvent) {
        this.channelEvent = channelEvent;
    }

}
