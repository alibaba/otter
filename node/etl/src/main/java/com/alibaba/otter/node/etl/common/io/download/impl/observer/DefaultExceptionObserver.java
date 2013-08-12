package com.alibaba.otter.node.etl.common.io.download.impl.observer;

import org.slf4j.Logger;

import com.alibaba.otter.node.etl.common.io.download.impl.AbstractCommandDownload;

/**
 * @author brave.taoy
 */
public class DefaultExceptionObserver extends ExceptionObserver {

    private Logger logger;

    public DefaultExceptionObserver(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void exceptionOccured(AbstractCommandDownload download, Exception status) {
        this.logger.error(status.getMessage(), status);
    }
}
