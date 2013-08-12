package com.alibaba.otter.node.etl.common.io.download.impl.observer;

import java.util.Observable;
import java.util.Observer;

import com.alibaba.otter.node.etl.common.io.download.impl.AbstractCommandDownload;

/**
 * @author brave.taoy
 */
public abstract class ProgressObserver implements Observer {

    public abstract void statusChanged(AbstractCommandDownload download, String status);

    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            statusChanged((AbstractCommandDownload) o, (String) arg);
        }
    }
}
