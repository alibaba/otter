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

package com.alibaba.otter.manager.web.home.module.action;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;

public class SwitchWarmupAction {

    @Resource(name = "channelService")
    private ChannelService         channelService;

    @Resource(name = "arbitrateManageService")
    private ArbitrateManageService arbitrateManageService;

    public void doSwitch(@Param("pipelineId") Long pipelineId, Navigator nav) throws Exception {
        Channel channel = channelService.findByPipelineId(pipelineId);
        arbitrateManageService.channelEvent().restart(channel.getId());// 尝试重新启动
        arbitrateManageService.systemEvent().switchWarmup(channel.getId(), pipelineId);
        nav.redirectToLocation("analysisStageStat.htm?pipelineId=" + pipelineId);
    }

    public void doRestart(@Param("pipelineId") Long pipelineId, Navigator nav) throws Exception {
        Channel channel = channelService.findByPipelineId(pipelineId);
        arbitrateManageService.channelEvent().restart(channel.getId());// 尝试重新启动
        channelService.notifyChannel(channel.getId());// 推送下配置
        nav.redirectToLocation("analysisStageStat.htm?pipelineId=" + pipelineId);
    }

}
