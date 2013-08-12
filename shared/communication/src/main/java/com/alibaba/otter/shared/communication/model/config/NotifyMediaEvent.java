package com.alibaba.otter.shared.communication.model.config;

import com.alibaba.otter.shared.communication.core.model.Event;

public class NotifyMediaEvent extends Event {

    private static final long serialVersionUID = -8472088519060045661L;

    public NotifyMediaEvent(){
        super(ConfigEventType.notifyMedia);
    }

    private String mediaInfo;

    public String getMediaInfo() {
        return mediaInfo;
    }

    public void setMediaInfo(String mediaInfo) {
        this.mediaInfo = mediaInfo;
    }

}
