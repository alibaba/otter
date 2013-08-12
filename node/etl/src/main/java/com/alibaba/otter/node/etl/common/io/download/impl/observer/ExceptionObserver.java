
package com.alibaba.otter.node.etl.common.io.download.impl.observer;

//~--- JDK imports ------------------------------------------------------------

import java.util.Observable;
import java.util.Observer;

import com.alibaba.otter.node.etl.common.io.download.impl.AbstractCommandDownload;

/**
 * @author     brave.taoy
 */
public abstract class ExceptionObserver implements Observer {
    public abstract void exceptionOccured(AbstractCommandDownload download, Exception status);

    public void update(Observable o, Object arg) {
        if (arg instanceof Exception) {
            exceptionOccured((AbstractCommandDownload) o, (Exception) arg);
        }
    }
}



