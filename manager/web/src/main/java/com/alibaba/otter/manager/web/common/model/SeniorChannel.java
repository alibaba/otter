package com.alibaba.otter.manager.web.common.model;

import com.alibaba.otter.shared.common.model.config.channel.Channel;

/**
 * @author simon 2012-1-13 下午03:34:32
 */
public class SeniorChannel extends Channel {

    private static final long serialVersionUID = -5864547001482768341L;
    private boolean           processEmpty;

    public boolean isProcessEmpty() {
        return processEmpty;
    }

    public void setProcessEmpty(boolean processEmpty) {
        this.processEmpty = processEmpty;
    }
}
