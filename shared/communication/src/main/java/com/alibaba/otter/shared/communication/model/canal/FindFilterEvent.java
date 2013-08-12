package com.alibaba.otter.shared.communication.model.canal;

import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 查询对应的过滤条件
 * 
 * @author jianghang 2012-7-23 下午02:41:52
 */
public class FindFilterEvent extends Event {

    private static final long serialVersionUID = 476657754177940448L;

    private String            destination;

    public FindFilterEvent(){
        super(CanalEventType.findFilter);
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

}
