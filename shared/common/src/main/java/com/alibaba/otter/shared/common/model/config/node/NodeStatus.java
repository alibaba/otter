package com.alibaba.otter.shared.common.model.config.node;

/**
 * Node节点的运行状态
 * 
 * @author jianghang
 */
public enum NodeStatus {

    /** 运行中 */
    START,
    /** 停止 */
    STOP;

    public boolean isStart() {
        return this.equals(NodeStatus.START);
    }

    public boolean isStop() {
        return this.equals(NodeStatus.STOP);
    }
}
