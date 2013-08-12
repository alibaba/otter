package com.alibaba.otter.shared.arbitrate.impl.setl.monitor.listener;

/**
 * 监控下permit发生变化，即时处理一些数据(比如select节点的启动)
 * 
 * @author jianghang 2011-12-8 下午04:48:14
 * @version 4.0.0
 */
public interface PermitListener {

    /**
     * 触发一下permit变化的值
     */
    public void processChanged(boolean isPermit);
}
