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

package com.alibaba.otter.shared.arbitrate.impl;

import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.PipelineArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.SystemArbitrateEvent;

/**
 * manager的管理信号
 * 
 * @author jianghang 2011-9-26 下午07:03:35
 * @version 4.0.0
 */
public class ArbitrateManageServiceImpl implements ArbitrateManageService {

    private SystemArbitrateEvent   systemEvent;
    private ChannelArbitrateEvent  channelEvent;
    private NodeArbitrateEvent     nodeEvent;
    private PipelineArbitrateEvent pipelineEvent;

    public SystemArbitrateEvent systemEvent() {
        return systemEvent;
    }

    public ChannelArbitrateEvent channelEvent() {
        return channelEvent;
    }

    public NodeArbitrateEvent nodeEvent() {
        return nodeEvent;
    }

    public PipelineArbitrateEvent pipelineEvent() {
        return pipelineEvent;
    }

    // ===================== setter / getter ===================

    public void setChannelEvent(ChannelArbitrateEvent channelEvent) {
        this.channelEvent = channelEvent;
    }

    public void setNodeEvent(NodeArbitrateEvent nodeEvent) {
        this.nodeEvent = nodeEvent;
    }

    public void setPipelineEvent(PipelineArbitrateEvent pipelineEvent) {
        this.pipelineEvent = pipelineEvent;
    }

    public void setSystemEvent(SystemArbitrateEvent systemEvent) {
        this.systemEvent = systemEvent;
    }

}
