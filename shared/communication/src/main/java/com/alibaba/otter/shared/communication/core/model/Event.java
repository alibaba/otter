package com.alibaba.otter.shared.communication.core.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * 通讯事件对象
 * 
 * @author jianghang 2011-9-9 下午04:02:53
 */
public abstract class Event implements Serializable {

    private static final long serialVersionUID = 208038167977229245L;

    private EventType         type;

    protected Event(){
    }

    protected Event(EventType type){
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
