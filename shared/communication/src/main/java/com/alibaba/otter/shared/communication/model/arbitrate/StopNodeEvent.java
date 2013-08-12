package com.alibaba.otter.shared.communication.model.arbitrate;

import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * node关闭的信号通知
 * 
 * @author jianghang 2012-8-29 下午02:03:15
 * @version 4.1.0
 */
public class StopNodeEvent extends Event {

    private static final long serialVersionUID = -8472088519060045661L;

    public StopNodeEvent(){
        super(ArbitrateEventType.stopNode);
    }

    private Long nid;

    public Long getNid() {
        return nid;
    }

    public void setNid(Long nid) {
        this.nid = nid;
    }

}
