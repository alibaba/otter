package com.alibaba.otter.shared.arbitrate.impl.setl.rpc.monitor;

import java.util.List;

/**
 * <pre>
 * 1. 合并S.E.T.L各类事件的监听，减少和zookeeper的交互
 * 2. 采用观察者事件变化推送的模式
 * </pre>
 * 
 * @author jianghang 2012-9-28 下午09:38:12
 * @version 4.1.0
 */
public interface ProcessListener {

    /**
     * 触发process变化，传递了变化后最新的processIds列表
     */
    public void processChanged(List<Long> processIds);

}
