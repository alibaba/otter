package com.alibaba.otter.shared.communication.app.event;

import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * @author jianghang 2011-9-13 下午08:31:36
 */
public class AppFindEvent extends Event {

    private static final long serialVersionUID = 810191575813164952L;

    public AppFindEvent(){
        super(AppEventType.find);
    }

    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
