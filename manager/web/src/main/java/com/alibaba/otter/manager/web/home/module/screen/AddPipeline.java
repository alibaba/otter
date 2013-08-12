package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.web.common.WebConstant;

public class AddPipeline {

    @Resource(name = "nodeService")
    private NodeService    nodeService;

    @Resource(name = "channelService")
    private ChannelService channelService;

    public void execute(@Param("channelId") Long channelId, Context context, Navigator nav) throws Exception {
        Channel channel = channelService.findById(channelId);
        if (channel.getStatus().isStart()) {
            nav.redirectTo(WebConstant.ERROR_FORBIDDEN_Link);
            return;
        }

        context.put("channelId", channelId);
        context.put("nodes", nodeService.listAll());
    }

}
