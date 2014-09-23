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

import com.alibaba.citrus.service.form.CustomErrors;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.node.NodeParameter;

public class NodeAction extends AbstractAction {

    @Resource(name = "nodeService")
    private NodeService              nodeService;

    @Resource(name = "pipelineService")
    private PipelineService          pipelineService;

    @Resource(name = "autoKeeperClusterService")
    private AutoKeeperClusterService autoKeeperClusterService;

    public void doAdd(@FormGroup("nodeInfo") Group nodeInfo, @FormGroup("nodeParameterInfo") Group nodeParameterInfo,
                      @FormField(name = "formNodeError", group = "nodeInfo") CustomErrors err, Navigator nav)
                                                                                                             throws Exception {
        Node node = new Node();
        NodeParameter parameter = new NodeParameter();
        nodeInfo.setProperties(node);
        nodeParameterInfo.setProperties(parameter);

        if (parameter.getDownloadPort() == null || parameter.getDownloadPort() == 0) {
            parameter.setDownloadPort(node.getPort().intValue() + 1);
        }

        if (parameter.getMbeanPort() == null || parameter.getMbeanPort() == 0) {
            parameter.setMbeanPort(node.getPort().intValue() + 2);
        }

        Long autoKeeperclusterId = nodeParameterInfo.getField("autoKeeperclusterId").getLongValue();
        if (autoKeeperclusterId != null && autoKeeperclusterId > 0) {
            AutoKeeperCluster autoKeeperCluster = autoKeeperClusterService.findAutoKeeperClusterById(autoKeeperclusterId);
            parameter.setZkCluster(autoKeeperCluster);
        }

        node.setParameters(parameter);
        try {
            nodeService.create(node);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidNode");
            return;
        }
        nav.redirectTo(WebConstant.NODE_LIST_LINK);
    }

    /**
     * 修改Node
     */
    public void doEdit(@FormGroup("nodeInfo") Group nodeInfo, @FormGroup("nodeParameterInfo") Group nodeParameterInfo,
                       @Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey,
                       @FormField(name = "formNodeError", group = "nodeInfo") CustomErrors err, Navigator nav)
                                                                                                              throws Exception {
        Node node = new Node();
        NodeParameter parameter = new NodeParameter();
        nodeInfo.setProperties(node);
        nodeParameterInfo.setProperties(parameter);

        if (parameter.getDownloadPort() == null || parameter.getDownloadPort() == 0) {
            parameter.setDownloadPort(node.getPort().intValue() + 1);
        }

        if (parameter.getMbeanPort() == null || parameter.getMbeanPort() == 0) {
            parameter.setMbeanPort(node.getPort().intValue() + 2);
        }

        Long autoKeeperclusterId = nodeParameterInfo.getField("autoKeeperclusterId").getLongValue();
        if (autoKeeperclusterId != null && autoKeeperclusterId > 0) {
            AutoKeeperCluster autoKeeperCluster = autoKeeperClusterService.findAutoKeeperClusterById(autoKeeperclusterId);
            parameter.setZkCluster(autoKeeperCluster);
        }

        node.setParameters(parameter);
        try {
            nodeService.modify(node);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidNode");
            return;
        }

        nav.redirectToLocation("nodeList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

    /**
     * 删除node
     * 
     * @param nodeId
     * @throws WebxException
     */
    public void doDelete(@Param("nodeId") Long nodeId, @Param("pageIndex") int pageIndex,
                         @Param("searchKey") String searchKey, Navigator nav) throws WebxException {

        if (pipelineService.hasRelation(nodeId) || nodeService.findById(nodeId).getStatus().isStart()) {
            nav.redirectTo(WebConstant.ERROR_FORBIDDEN_Link);
            return;
        } else {
            nodeService.remove(nodeId);
        }

        nav.redirectToLocation("nodeList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }
}
