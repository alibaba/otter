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

package com.alibaba.otter.shared.arbitrate.impl.setl.lb;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateLifeCycle;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.NodeMonitor;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * 抽象的负载均衡接口
 * 
 * @author jianghang 2011-9-20 下午01:31:29
 * @version 4.0.0
 */
public abstract class AbstractLoadBalance extends ArbitrateLifeCycle implements LoadBalance {

    protected NodeMonitor nodeMonitor;

    public AbstractLoadBalance(Long pipelineId){
        super(pipelineId);
    }

    public abstract List<Node> getAliveNodes();

    public void destory() {
        super.destory();
    }

    public void setNodeMonitor(NodeMonitor nodeMonitor) {
        this.nodeMonitor = nodeMonitor;
    }

    public List<Node> getExtractAliveNodes() {
        Pipeline pipeline = ArbitrateConfigUtils.getPipeline(getPipelineId());
        List<Node> extractNodes = pipeline.getExtractNodes();
        List<Node> eNodes = new ArrayList<Node>();

        List<Long> aliveNodes = nodeMonitor.getAliveNodes();
        for (Node sourceNode : extractNodes) {
            if (aliveNodes.contains(sourceNode.getId())) {
                eNodes.add(sourceNode);
            }
        }

        return eNodes;
    }

    public List<Node> getTransformAliveNodes() {
        Pipeline pipeline = ArbitrateConfigUtils.getPipeline(getPipelineId());
        List<Node> transformNodes = pipeline.getLoadNodes();
        List<Node> tNodes = new ArrayList<Node>();

        List<Long> aliveNodes = nodeMonitor.getAliveNodes();
        for (Node sourceNode : transformNodes) {
            if (aliveNodes.contains(sourceNode.getId())) {
                tNodes.add(sourceNode);
            }
        }

        return tNodes;
    }

}
