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
