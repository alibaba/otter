package com.alibaba.otter.manager.biz.common.alarm;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

public class AlarmMessage implements Serializable {

    private static final long serialVersionUID = 6110474591366995515L;
    private String            message;
    private String            receiveKey;

    public AlarmMessage(){

    }

    public AlarmMessage(String message, String receiveKey){
        this.message = message;
        this.receiveKey = receiveKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiveKey() {
        return receiveKey;
    }

    public void setReceiveKey(String receiveKey) {
        this.receiveKey = receiveKey;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }

}
