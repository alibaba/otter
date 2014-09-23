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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.LoadArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.enums.StageType;

/**
 * 基于memory的仲裁器实现
 * 
 * @author jianghang 2012-9-27 下午10:05:08
 * @version 4.1.0
 */
public class LoadMemoryArbitrateEvent implements LoadArbitrateEvent {

    private static final Logger        logger = LoggerFactory.getLogger(LoadMemoryArbitrateEvent.class);
    private TerminMemoryArbitrateEvent terminEvent;

    public EtlEventData await(Long pipelineId) throws InterruptedException {
        Assert.notNull(pipelineId);

        PermitMonitor permitMonitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        permitMonitor.waitForPermit();// 阻塞等待授权

        MemoryStageController stageController = ArbitrateFactory.getInstance(pipelineId, MemoryStageController.class);
        Long processId = stageController.waitForProcess(StageType.LOAD); // 符合条件的processId

        ChannelStatus status = permitMonitor.getChannelPermit();
        if (status.isStart()) {// 即时查询一下当前的状态，状态随时可能会变
            return stageController.getLastData(processId);
        } else {
            logger.warn("pipelineId[{}] load ignore processId[{}] by status[{}]", new Object[] { pipelineId, processId,
                    status });
            return await(pipelineId);// 递归调用
        }
    }

    public void single(EtlEventData data) {
        Assert.notNull(data);
        data.setEndTime(new Date().getTime());// 返回当前时间
        MemoryStageController stageController = ArbitrateFactory.getInstance(data.getPipelineId(),
                                                                             MemoryStageController.class);
        boolean result = stageController.single(StageType.LOAD, data);// 通知下一个节点
        if (result) {// 可能已经被rollback了，需要直接忽略
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
    }

    public void setTerminEvent(TerminMemoryArbitrateEvent terminEvent) {
        this.terminEvent = terminEvent;
    }

}
