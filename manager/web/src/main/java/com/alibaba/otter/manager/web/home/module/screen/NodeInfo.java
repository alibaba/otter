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
