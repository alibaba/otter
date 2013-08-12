package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;

public class PipelineInfo {

    @Resource(name = "pipelineService")
    private PipelineService pipelineService;

    @Resource(name = "nodeService")
    private NodeService     nodeService;

    public void execute(@Param("pipelineId") Long pipelineId, Context context) throws Exception {
        Pipeline pipeline = pipelineService.findById(pipelineId);

        context.put("pipeline", pipeline);
        context.put("nodes", nodeService.listAll());
    }
}
