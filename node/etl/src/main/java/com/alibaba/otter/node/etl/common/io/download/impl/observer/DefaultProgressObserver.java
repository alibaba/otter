package com.alibaba.otter.node.etl.common.io.download.impl.observer;

import org.slf4j.Logger;

import com.alibaba.otter.node.etl.common.io.download.impl.AbstractCommandDownload;

public class DefaultProgressObserver extends ProgressObserver {

    private Logger logger;

    public DefaultProgressObserver(Logger logger){
        this.logger = logger;
    }

    @Override
    public void statusChanged(AbstractCommandDownload download, String msg) {
        if (logger.isInfoEnabled()) {
            logger.info(msg);
        }
    }
}
