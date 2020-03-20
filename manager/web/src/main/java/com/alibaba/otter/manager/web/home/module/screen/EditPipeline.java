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
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.arbitrate.ArbitrateViewService;
import com.alibaba.otter.shared.arbitrate.model.PositionEventData;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

public class EditPipeline {

    @Resource(name = "pipelineService")
    private PipelineService pipelineService;
    @Resource(name = "nodeService")
    private NodeService     nodeService;
    @Resource(name = "channelService")
    private ChannelService  channelService;
    @Resource
    private ArbitrateViewService arbitrateViewService;

    /**
     * 找到单个Channel，用于编辑Channel信息界面加载信息
     * 
     * @param pipelineId
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
        // 返回canal当前位点信息
        PositionEventData positionEventData = arbitrateViewService.getCanalCursor(pipeline.getParameters().getDestinationName(), pipeline.getParameters().
                getMainstemClientId());
        if (null != positionEventData) {
            pipeline.setPosition(positionEventData.getPosition());
        }
        context.put("pipeline", pipeline);
        context.put("nodes", nodeService.listAll());
    }
}
