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

import java.util.List;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.remote.NodeRemoteService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;

public class NodeInfo {

    @Resource(name = "nodeService")
    private NodeService       nodeService;

    @Resource(name = "channelService")
    private ChannelService    channelService;

    @Resource(name = "nodeRemoteService")
    private NodeRemoteService nodeRemoteService;

    public void execute(@Param("nodeId") Long nodeId, Context context) throws Exception {
        Node node = nodeService.findById(nodeId);
        List<Channel> channels = channelService.listByNodeId(nodeId);
        if (node.getStatus().isStart()) {
            context.put("heapMemoryUsage", nodeRemoteService.getHeapMemoryUsage(nodeId));
            context.put("versionInfo", nodeRemoteService.getNodeVersionInfo(nodeId));
            context.put("systemInfo", nodeRemoteService.getNodeSystemInfo(nodeId));
            context.put("threadActiveSize", nodeRemoteService.getThreadActiveSize(nodeId));
            context.put("threadPoolSize", nodeRemoteService.getThreadPoolSize(nodeId));
            context.put("runningPipelines", nodeRemoteService.getRunningPipelines(nodeId));
        } else {
            context.put("heapMemoryUsage", 0);
            context.put("threadActiveSize", 0);
            context.put("threadPoolSize", 0);
            context.put("runningPipelines", 0);
            context.put("versionInfo", "");
            context.put("systemInfo", "");
        }
        context.put("node", node);
        context.put("channels", channels);
    }
}
