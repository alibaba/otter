package com.alibaba.otter.shared.arbitrate.impl.setl.monitor.listener;

import java.util.List;

/**
 * dead node的监控处理实现，运行在启动了mainStem的单节点上，避免多个节点同时处理，所以抽取了Listener
 * 
 * @author jianghang 2011-9-26 下午10:33:42
 * @version 4.0.0
 */
public interface NodeListener {

    /**
     * 触发process变化，传递了变化后最新的processIds列表
     */
    public void processChanged(List<Long> aliveNodes);

}
