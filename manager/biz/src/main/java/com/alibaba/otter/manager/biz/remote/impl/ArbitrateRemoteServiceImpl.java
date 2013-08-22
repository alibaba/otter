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

package com.alibaba.otter.manager.biz.remote.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.monitor.PassiveMonitor;
import com.alibaba.otter.manager.biz.remote.ArbitrateRemoteService;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.communication.core.CommunicationRegistry;
import com.alibaba.otter.shared.communication.model.arbitrate.ArbitrateEventType;
import com.alibaba.otter.shared.communication.model.arbitrate.NodeAlarmEvent;
import com.alibaba.otter.shared.communication.model.arbitrate.StopChannelEvent;
import com.alibaba.otter.shared.communication.model.arbitrate.StopNodeEvent;

/**
 * 处理仲裁器事件的远程接口
 * 
 * @author jianghang 2011-11-24 下午09:19:09
 * @version 4.0.0
 */
public class ArbitrateRemoteServiceImpl implements ArbitrateRemoteService {

    private static final Logger    logger = LoggerFactory.getLogger(ArbitrateRemoteServiceImpl.class);
    private ArbitrateManageService arbitrateManageService;
    private ChannelService         channelService;
    private PassiveMonitor         exceptionRuleMonitor;

    public ArbitrateRemoteServiceImpl(){
        CommunicationRegistry.regist(ArbitrateEventType.nodeAlarm, this);
        CommunicationRegistry.regist(ArbitrateEventType.stopChannel, this);
        CommunicationRegistry.regist(ArbitrateEventType.stopNode, this);
    }

    public void onNodeAlarm(NodeAlarmEvent event) {
        try {
            exceptionRuleMonitor.feed(event, event.getPipelineId());
        } catch (Exception e) {
            logger.error(String.format("ERROR # exceptionRuleMonitor error for  %s", event.toString()), e);
        }
    }

    public void onStopChannel(StopChannelEvent event) {
        channelService.stopChannel(event.getChannelId());
    }

    public void onStopNode(StopNodeEvent event) {
        Assert.notNull(event);

        List<Channel> channels = channelService.listByNodeId(event.getNid(), ChannelStatus.START);
        for (Channel channel : channels) {// 重启一下对应的channel
            boolean result = arbitrateManageService.channelEvent().restart(channel.getId());
            if (result) {
                channelService.notifyChannel(channel.getId());// 推送一下配置
            }
        }
    }

    // ===================== setter / getter =====================

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public void setArbitrateManageService(ArbitrateManageService arbitrateManageService) {
        this.arbitrateManageService = arbitrateManageService;
    }

    public void setExceptionRuleMonitor(PassiveMonitor exceptionRuleMonitor) {
        this.exceptionRuleMonitor = exceptionRuleMonitor;
    }

}
