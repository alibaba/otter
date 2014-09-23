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

package com.alibaba.otter.manager.web.home.module.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.manager.biz.config.canal.CanalService;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

public class CanalInfo {

    @Resource(name = "canalService")
    private CanalService             canalService;

    @Resource(name = "autoKeeperClusterService")
    private AutoKeeperClusterService autoKeeperClusterService;

    @Resource(name = "pipelineService")
    private PipelineService          pipelineService;

    @Resource(name = "channelService")
    private ChannelService           channelService;

    public void execute(@Param("canalId") Long canalId, Context context) throws Exception {
        Canal canal = canalService.findById(canalId);
        AutoKeeperCluster zkCluster = autoKeeperClusterService.findAutoKeeperClusterById(canal.getCanalParameter()
            .getZkClusterId());

        List<Pipeline> pipelines = pipelineService.listByDestinationWithoutOther(canal.getName());
        List<Long> channelIds = new ArrayList<Long>();
        for (Pipeline pipeline : pipelines) {
            channelIds.add(pipeline.getChannelId());
        }

        List<Channel> channels = channelService.listOnlyChannels(channelIds.toArray(new Long[channelIds.size()]));
        Map<Long, Channel> channelMap = new HashMap<Long, Channel>();
        for (Channel channel : channels) {
            channelMap.put(channel.getId(), channel);
        }

        context.put("canal", canal);
        context.put("pipelines", pipelines);
        context.put("channelMap", channelMap);
        context.put("zkCluster", zkCluster);
    }
}
