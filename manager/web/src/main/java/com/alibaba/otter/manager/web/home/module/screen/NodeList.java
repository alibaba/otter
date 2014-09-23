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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.web.common.model.SeniorNode;
import com.alibaba.otter.shared.common.model.config.node.Node;

/**
 * 类NodeList.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-10-25 上午10:25:27
 */
public class NodeList {

    @Resource(name = "nodeService")
    private NodeService     nodeService;

    @Resource(name = "pipelineService")
    private PipelineService pipelineService;

    public void execute(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey, Context context)
                                                                                                                 throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> condition = new HashMap<String, Object>();
        if ("请输入关键字(目前支持Node的ID、名字搜索)".equals(searchKey)) {
            searchKey = "";
        }
        condition.put("searchKey", searchKey);

        int count = nodeService.getCount(condition);
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());

        List<Node> nodes = nodeService.listByCondition(condition);

        List<SeniorNode> seniorNodes = new ArrayList<SeniorNode>();

        for (Node node : nodes) {
            SeniorNode seniorNode = new SeniorNode();
            seniorNode.setId(node.getId());
            seniorNode.setIp(node.getIp());
            seniorNode.setName(node.getName());
            seniorNode.setPort(node.getPort());
            seniorNode.setDescription(node.getDescription());
            seniorNode.setStatus(node.getStatus());
            seniorNode.setParameters(node.getParameters());
            seniorNode.setGmtCreate(node.getGmtCreate());
            seniorNode.setGmtModified(node.getGmtModified());
            seniorNode.setUsed(pipelineService.hasRelation(node.getId()));
            seniorNodes.add(seniorNode);
        }
        context.put("seniorNodes", seniorNodes);
        context.put("paginator", paginator);
        context.put("searchKey", searchKey);
    }
}
