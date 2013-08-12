package com.alibaba.otter.shared.arbitrate.model;

import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter.ArbitrateMode;

/**
 * process node节点使用的数据对象
 * 
 * @author jianghang 2011-12-1 下午06:23:40
 * @version 4.0.0
 */
public class ProcessNodeEventData extends EventData {

    private static final long serialVersionUID = -7622558087796345197L;

    public enum Status {
        /** 已使用 */
        USED,
        /** 未使用 */
        UNUSED;

        public boolean isUsed() {
            return this == USED;
        }

        public boolean isUnUsed() {
            return this == UNUSED;
        }
    }

    private Long          nid;
    private Status        status;
    private ArbitrateMode mode = ArbitrateMode.ZOOKEEPER;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getNid() {
        return nid;
    }

    public void setNid(Long nid) {
        this.nid = nid;
    }

    public ArbitrateMode getMode() {
        return mode;
    }

    public void setMode(ArbitrateMode mode) {
        this.mode = mode;
    }

}
