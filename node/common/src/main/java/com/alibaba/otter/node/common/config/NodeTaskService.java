package com.alibaba.otter.node.common.config;

import java.util.List;

import com.alibaba.otter.node.common.config.model.NodeTask;

/**
 * Node节点任务分发的服务类
 * 
 * @author jianghang
 */
public interface NodeTaskService {

    /**
     * 根据对应的获取任务列表，<strong>注意是所有的任务</strong>
     */
    public List<NodeTask> listAllNodeTasks();

    /**
     * 注册监听器
     */
    public void addListener(NodeTaskListener listener);

    /**
     * 关闭node
     */
    public void stopNode();

}
