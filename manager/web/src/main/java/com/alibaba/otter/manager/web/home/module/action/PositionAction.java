package com.alibaba.otter.manager.web.home.module.action;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.shared.arbitrate.ArbitrateViewService;
import com.alibaba.otter.shared.arbitrate.model.PositionEventData;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

public class PositionAction {

    private static final Logger  logger = LoggerFactory.getLogger(PositionAction.class);

    @Resource(name = "pipelineService")
    private PipelineService      pipelineService;

    @Resource(name = "arbitrateViewService")
    private ArbitrateViewService arbitrateViewService;

    public void doRemove(@Param("pipelineId") Long pipelineId, Navigator nav) throws Exception {
        Pipeline pipeline = pipelineService.findById(pipelineId);
        String destination = pipeline.getParameters().getDestinationName();
        short clientId = pipeline.getParameters().getMainstemClientId();
        PositionEventData position = arbitrateViewService.getCanalCursor(destination, clientId);
        logger.warn("remove pipelineId[{}] position \n {}", pipelineId, position); // 记录一下日志
        arbitrateViewService.removeCanalCursor(destination, clientId);
        nav.redirectToLocation("analysisStageStat.htm?pipelineId=" + pipelineId);
    }
}
