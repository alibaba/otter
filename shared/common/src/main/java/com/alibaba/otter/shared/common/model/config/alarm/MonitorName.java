package com.alibaba.otter.shared.common.model.config.alarm;

/**
 * @author simon 2012-8-29 下午7:33:53
 * @version 4.1.0
 */
public enum MonitorName {
    /** 挂起异常 */
    PAUSED,

    /** 堆积 */
    QUEUESIZE,

    /** 延迟 */
    DELAYTIME,

    /** 异常 */
    EXCEPTION,

    /** Pipeline超时 */
    PIPELINETIMEOUT,

    /** Process超时 */
    PROCESSTIMEOUT,

    /** position超时 */
    POSITIONTIMEOUT;

    public boolean isPaused() {
        return this.equals(MonitorName.PAUSED);
    }

    public boolean isQueueSize() {
        return this.equals(MonitorName.QUEUESIZE);
    }

    public boolean isDelayTime() {
        return this.equals(MonitorName.DELAYTIME);
    }

    public boolean isPipelineTimeout() {
        return this.equals(MonitorName.PIPELINETIMEOUT);
    }

    public boolean isProcessTimeout() {
        return this.equals(MonitorName.PROCESSTIMEOUT);
    }

    public boolean isException() {
        return this.equals(MonitorName.EXCEPTION);
    }

    public boolean isPositionTimeout() {
        return this.equals(MonitorName.POSITIONTIMEOUT);
    }
}
