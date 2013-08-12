package com.alibaba.otter.shared.arbitrate.impl.setl.lb;

import java.util.List;

import com.alibaba.otter.shared.common.model.config.node.Node;

public class ExtractStickLoadBalance extends StickLoadBalance {

    public ExtractStickLoadBalance(Long pipelineId){
        super(pipelineId);
    }

    @Override
    public List<Node> getAliveNodes() {
        return getExtractAliveNodes();
    }

}
