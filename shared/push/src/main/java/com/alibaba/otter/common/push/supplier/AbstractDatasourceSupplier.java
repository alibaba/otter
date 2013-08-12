package com.alibaba.otter.common.push.supplier;

/**
 * @author zebin.xuzb 2013-1-23 下午5:08:51
 * @since 4.1.3
 */
public abstract class AbstractDatasourceSupplier implements DatasourceSupplier {

    private Object             lock    = new Object();
    protected volatile boolean running = false;

    @Override
    public void start() {
        synchronized (lock) {
            if (isStart()) {
                return;
            }
            doStart();
            running = true;
        }
    }

    @Override
    public void stop() {
        synchronized (lock) {
            if (!isStart()) {
                return;
            }
            doStop();
            running = false;
        }
    }

    @Override
    public boolean isStart() {
        return running;
    }

    protected abstract void doStart();

    protected abstract void doStop();
}
