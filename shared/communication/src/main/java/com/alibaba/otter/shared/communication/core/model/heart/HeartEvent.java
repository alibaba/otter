package com.alibaba.otter.shared.communication.core.model.heart;

import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.communication.core.model.EventType;

/**
 * 心跳检查事件
 * 
 * @author jianghang
 */
public class HeartEvent extends Event {

    private static final long serialVersionUID = 8690886624112649424L;

    public HeartEvent(){
        super(HeartEventType.HEARTBEAT);
    }

    private Byte heart = 1;

    public static enum HeartEventType implements EventType {
        HEARTBEAT;
    }

    public Byte getHeart() {
        return heart;
    }

    public void setHeart(Byte heart) {
        this.heart = heart;
    }

}
