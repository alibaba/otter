package com.alibaba.otter.node.etl.common.io.download.impl.observer;

import org.slf4j.Logger;

import com.alibaba.otter.node.etl.common.io.download.Download;
import com.alibaba.otter.node.etl.common.io.download.DownloadStatus;

/**
 * @author brave.taoy
 */
public class DefaultStatusObserver extends StatusObserver {

    private Logger logger;

    public DefaultStatusObserver(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void statusChanged(Download download, DownloadStatus status) {
        if (logger.isInfoEnabled()) {
            logger.info("status: " + status.name());
        }
    }
}
