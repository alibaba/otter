package com.alibaba.otter.shared.arbitrate.impl.setl;

/**
 * @author jianghang 2011-9-20 下午01:05:24
 * @version 4.0.0
 */
public abstract class ArbitrateLifeCycle {

    private Long             pipelineId;
    private volatile boolean stop = false; //是否关闭

    public ArbitrateLifeCycle(Long pipelineId){
        this.pipelineId = pipelineId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public void destory() {
        stop = true;
    }
}
