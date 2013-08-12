package com.alibaba.otter.shared.arbitrate.impl.setl.lb;

import java.util.List;

import com.alibaba.otter.shared.common.model.config.node.Node;

/**
 * transform模块的负载均衡实现
 * 
 * @author jianghang 2011-9-20 下午01:24:22
 * @version 4.0.0
 */
public class TransformRoundRobinLoadBalance extends RoundRobinLoadBalance {

    public TransformRoundRobinLoadBalance(Long pipelineId){
        super(pipelineId);
    }

    @Override
    public List<Node> getAliveNodes() {
        return getTransformAliveNodes();
    }

}
