package com.alibaba.otter.shared.communication.model.canal;

import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 配置查询的事件
 * 
 * @author jianghang
 */
public class FindCanalEvent extends Event {

    private static final long serialVersionUID = 476657754177940448L;

    private String            destination;

    public FindCanalEvent(){
        super(CanalEventType.findCanal);
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

}
