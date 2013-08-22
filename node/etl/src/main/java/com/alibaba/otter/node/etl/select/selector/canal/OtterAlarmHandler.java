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

package com.alibaba.otter.node.etl.select.selector.canal;

import com.alibaba.otter.canal.common.AbstractCanalLifeCycle;
import com.alibaba.otter.canal.common.alarm.CanalAlarmHandler;
import com.alibaba.otter.shared.arbitrate.ArbitrateEventService;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;

/**
 * 基于otter manager的报警机制实现
 * 
 * @author jianghang 2012-8-23 上午10:59:58
 * @version 4.1.0
 */
public class OtterAlarmHandler extends AbstractCanalLifeCycle implements CanalAlarmHandler {

    private Long                  pipelineId;
    private ArbitrateEventService arbitrateEventService;

    public void sendAlarm(String destination, String msg) {
        TerminEventData errorEventData = new TerminEventData();
        errorEventData.setPipelineId(pipelineId);
        errorEventData.setType(TerminType.WARNING);
        errorEventData.setCode("canal");
        errorEventData.setDesc(destination + ":" + msg);
        arbitrateEventService.terminEvent().single(errorEventData);
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public void setArbitrateEventService(ArbitrateEventService arbitrateEventService) {
        this.arbitrateEventService = arbitrateEventService;
    }

}
