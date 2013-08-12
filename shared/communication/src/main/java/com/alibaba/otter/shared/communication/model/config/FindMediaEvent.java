package com.alibaba.otter.shared.communication.model.config;

import com.alibaba.otter.shared.communication.core.model.Event;

public class FindMediaEvent extends Event {

    private static final long serialVersionUID = 476657754177940448L;

    private String            dataId;

    public FindMediaEvent(){
        super(ConfigEventType.findMedia);
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

}
