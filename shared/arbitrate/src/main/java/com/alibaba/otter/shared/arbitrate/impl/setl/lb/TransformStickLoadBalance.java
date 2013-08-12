package com.alibaba.otter.shared.arbitrate.impl.setl.lb;

import java.util.List;

import com.alibaba.otter.shared.common.model.config.node.Node;

public class TransformStickLoadBalance extends StickLoadBalance {

    public TransformStickLoadBalance(Long pipelineId){
        super(pipelineId);
    }

    public List<Node> getAliveNodes() {
        return getTransformAliveNodes();
    }
}
