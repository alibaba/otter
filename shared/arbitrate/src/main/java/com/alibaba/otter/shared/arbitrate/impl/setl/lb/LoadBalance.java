package com.alibaba.otter.shared.arbitrate.impl.setl.lb;

import com.alibaba.otter.shared.common.model.config.node.Node;

/**
 * 负载均衡算法
 * 
 * @author jianghang 2011-8-19 上午10:16:45
 */
public interface LoadBalance {

    public Node next() throws InterruptedException;
}
