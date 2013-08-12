package com.alibaba.otter.node.etl.common.io.download.impl.observer;

import java.util.Observable;
import java.util.Observer;

import com.alibaba.otter.node.etl.common.io.download.Download;
import com.alibaba.otter.node.etl.common.io.download.DownloadStatus;

/**
 * @author brave.taoy
 */
public abstract class StatusObserver implements Observer {

    public abstract void statusChanged(Download download, DownloadStatus status);

    public void update(Observable o, Object arg) {
        if (arg instanceof DownloadStatus) {
            statusChanged((Download) o, (DownloadStatus) arg);
        }
    }
}
