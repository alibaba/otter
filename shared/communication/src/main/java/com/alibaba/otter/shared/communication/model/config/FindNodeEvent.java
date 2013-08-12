package com.alibaba.otter.shared.communication.model.config;

import com.alibaba.otter.shared.communication.core.model.Event;

public class FindNodeEvent extends Event {

    private static final long serialVersionUID = 476657754177940448L;

    private Long              nid;                                   // 对应的task机器id

    public FindNodeEvent(){
        super(ConfigEventType.findNode);
    }

    public Long getNid() {
        return nid;
    }

    public void setNid(Long nid) {
        this.nid = nid;
    }

}
