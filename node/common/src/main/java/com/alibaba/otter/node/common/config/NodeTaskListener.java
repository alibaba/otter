package com.alibaba.otter.node.common.config;

import java.util.List;

import com.alibaba.otter.node.common.config.model.NodeTask;

/**
 * 在nodeTask发生变化时，广播通知下
 * 
 * @author jianghang 2012-4-20 下午10:45:17
 * @version 4.0.2
 */
public interface NodeTaskListener {

    boolean process(List<NodeTask> nodeTasks);
}
