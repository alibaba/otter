package com.alibaba.otter.shared.communication.model.config;

import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 配置查询的事件
 * 
 * @author jianghang
 */
public class FindTaskEvent extends Event {

    private static final long serialVersionUID = 476657754177940448L;

    private Long              nid;                                   // 对应的task机器id

    public FindTaskEvent(){
        super(ConfigEventType.findTask);
    }

    public Long getNid() {
        return nid;
    }

    public void setNid(Long nid) {
        this.nid = nid;
    }

}
