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
