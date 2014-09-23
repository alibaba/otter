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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;

import com.alibaba.citrus.service.form.CustomErrors;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter;

public class PipelineAction {

    @Resource(name = "pipelineService")
    private PipelineService      pipelineService;

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService dataMediaPairService;

    @Resource(name = "channelService")
    private ChannelService       channelService;

    public void doAdd(@FormGroup("pipelineInfo") Group pipelineInfo,
                      @FormGroup("pipelineParameterInfo") Group pipelineParameterInfo,
                      @FormField(name = "formPipelineError", group = "pipelineInfo") CustomErrors err,
                      HttpSession session, Navigator nav) throws Exception {
        Pipeline pipeline = new Pipeline();
        PipelineParameter parameters = new PipelineParameter();
        pipelineInfo.setProperties(pipeline);
        pipelineParameterInfo.setProperties(parameters);
        // if (parameters.getLoadPoolSize() < 1) {
        // parameters.setLoadPoolSize(PipelineParameter.DEFAULT_LOAD_POOL_SIZE);
        // }

        List<Long> selectNodeIds = Arrays.asList(ArrayUtils.toObject(pipelineInfo.getField("selectNodeIds")
            .getLongValues()));
        List<Node> selectNodes = new ArrayList<Node>();
        for (Long selectNodeId : selectNodeIds) {
            Node node = new Node();
            node.setId(selectNodeId);
            selectNodes.add(node);
        }

        // select/extract节点普遍配置为同一个节点
        List<Long> extractNodeIds = Arrays.asList(ArrayUtils.toObject(pipelineInfo.getField("selectNodeIds")
            .getLongValues()));
        // List<Long> extractNodeIds =
        // Arrays.asList(ArrayUtils.toObject(pipelineInfo.getField("extractNodeIds").getLongValues()));
        List<Node> extractNodes = new ArrayList<Node>();
        for (Long extractNodeId : extractNodeIds) {
            Node node = new Node();
            node.setId(extractNodeId);
            extractNodes.add(node);
        }

        List<Long> loadNodeIds = Arrays.asList(ArrayUtils.toObject(pipelineInfo.getField("loadNodeIds").getLongValues()));
        List<Node> loadNodes = new ArrayList<Node>();
        for (Long loadNodeId : loadNodeIds) {
            Node node = new Node();
            node.setId(loadNodeId);
            loadNodes.add(node);
        }

        pipeline.setSelectNodes(selectNodes);
        pipeline.setExtractNodes(extractNodes);
        pipeline.setLoadNodes(loadNodes);
        pipeline.setParameters(parameters);

        List<Pipeline> values = pipelineService.listByDestinationWithoutOther(pipeline.getParameters()
            .getDestinationName());
        if (!values.isEmpty()) {
            err.setMessage("invalidDestinationName");
            return;
        }

        try {
            pipelineService.create(pipeline);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidPipelineName");
            return;
        }
        nav.redirectToLocation("pipelineList.htm?channelId=" + pipeline.getChannelId());
    }

    public void doDelete(@Param("pipelineId") Long pipelineId, @Param("channelId") Long channelId, Navigator nav)
                                                                                                                 throws WebxException {
        Channel channel = channelService.findById(channelId);
        if (channel.getStatus().isStart()) {
            nav.redirectTo(WebConstant.ERROR_FORBIDDEN_Link);
            return;
        }
        // 如果pipeline节点下面存在dataMediaPair，则不允许删除
        if (dataMediaPairService.listByPipelineId(pipelineId).size() < 1) {
            pipelineService.remove(pipelineId);
        }

        nav.redirectToLocation("pipelineList.htm?channelId=" + channelId);
    }

    public void doEdit(@FormGroup("pipelineInfo") Group pipelineInfo,
                       @FormGroup("pipelineParameterInfo") Group pipelineParameterInfo,
                       @FormField(name = "formPipelineError", group = "pipelineInfo") CustomErrors err,
                       HttpSession session, Navigator nav) {
        Pipeline pipeline = new Pipeline();
        PipelineParameter parameters = new PipelineParameter();
        pipelineInfo.setProperties(pipeline);
        pipelineParameterInfo.setProperties(parameters);
        // if (parameters.getLoadPoolSize() < 1) {
        // parameters.setLoadPoolSize(PipelineParameter.DEFAULT_LOAD_POOL_SIZE);
        // }

        List<Long> selectNodeIds = Arrays.asList(ArrayUtils.toObject(pipelineInfo.getField("selectNodeIds")
            .getLongValues()));
        List<Node> selectNodes = new ArrayList<Node>();
        for (Long selectNodeId : selectNodeIds) {
            Node node = new Node();
            node.setId(selectNodeId);
            selectNodes.add(node);
        }

        // select/extract节点普遍配置为同一个节点
        List<Long> extractNodeIds = Arrays.asList(ArrayUtils.toObject(pipelineInfo.getField("selectNodeIds")
            .getLongValues()));
        // List<Long> extractNodeIds =
        // Arrays.asList(ArrayUtils.toObject(pipelineInfo.getField("extractNodeIds").getLongValues()));
        List<Node> extractNodes = new ArrayList<Node>();
        for (Long extractNodeId : extractNodeIds) {
            Node node = new Node();
            node.setId(extractNodeId);
            extractNodes.add(node);
        }

        List<Long> loadNodeIds = Arrays.asList(ArrayUtils.toObject(pipelineInfo.getField("loadNodeIds").getLongValues()));
        List<Node> loadNodes = new ArrayList<Node>();
        for (Long loadNodeId : loadNodeIds) {
            Node node = new Node();
            node.setId(loadNodeId);
            loadNodes.add(node);
        }

        pipeline.setSelectNodes(selectNodes);
        pipeline.setExtractNodes(extractNodes);
        pipeline.setLoadNodes(loadNodes);
        pipeline.setParameters(parameters);

        List<Pipeline> values = pipelineService.listByDestinationWithoutOther(pipeline.getParameters()
            .getDestinationName());

        if (!values.isEmpty()) {
            if (values.size() > 1 || !values.get(0).getId().equals(pipeline.getId())) {
                err.setMessage("invalidDestinationName");
                return;
            }
        }

        try {
            pipelineService.modify(pipeline);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidPipelineName");
            return;
        }
        nav.redirectToLocation("pipelineList.htm?channelId=" + pipeline.getChannelId());
    }

}
