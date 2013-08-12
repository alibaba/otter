package com.alibaba.otter.shared.common.model.config.data.mq;

import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;

/**
 * NapoliConnector对象的实现
 * 
 * @author simon 2012-6-19 下午10:49:25
 * @version 4.1.0
 */
public class MqMediaSource extends DataMediaSource {

    private static final long serialVersionUID = -1699317916850638142L;
    private String            url;
    private String            storePath;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

}
