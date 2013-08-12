package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

public class EditPipeline {

    @Resource(name = "pipelineService")
    private PipelineService pipelineService;
    @Resource(name = "nodeService")
    private NodeService     nodeService;
    @Resource(name = "channelService")
    private ChannelService  channelService;

    /**
     * 找到单个Channel，用于编辑Channel信息界面加载信息
     * 
     * @param channelId
     * @param context
     * @throws WebxException
     */
    public void execute(@Param("pipelineId") Long pipelineId, Context context, Navigator nav) throws Exception {
        Channel channel = channelService.findByPipelineId(pipelineId);
        if (channel.getStatus().isStart()) {
            nav.redirectTo(WebConstant.ERROR_FORBIDDEN_Link);
            return;
        }

        Pipeline pipeline = pipelineService.findById(pipelineId);
        context.put("pipeline", pipeline);
        context.put("nodes", nodeService.listAll());
    }
}
