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
