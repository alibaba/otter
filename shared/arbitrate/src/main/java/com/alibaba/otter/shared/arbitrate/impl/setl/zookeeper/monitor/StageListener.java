package com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor;

import java.util.List;

/**
 * S.E.T.L模块监控的实现
 * 
 * <pre>
 * 1. 合并S.E.T.L各类事件的监听，减少和zookeeper的交互
 * 2. 采用观察者事件变化推送的模式
 * </pre>
 * 
 * @author jianghang 2011-9-21 上午10:58:20
 * @version 4.0.0
 */
public interface StageListener {

    /**
     * 触发process变化，传递了变化后最新的processIds列表
     */
    public void processChanged(List<Long> processIds);

    /**
     * process节点被删除，触发对应的事件
     */
    public void processTermined(Long processId);

    /**
     * 单个process stage节点发生变化，传递了变化后最新的stage列表
     */
    public void stageChannged(Long processId, List<String> stageNodes);
}
