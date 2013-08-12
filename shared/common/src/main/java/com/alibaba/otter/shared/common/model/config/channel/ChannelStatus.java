package com.alibaba.otter.shared.common.model.config.channel;

/**
 * channel的运行状态
 * 
 * @author jianghang
 */
public enum ChannelStatus {
    /** 运行中 */
    START,
    /** 暂停(临时停止) */
    PAUSE,
    /** 停止(长时停止) */
    STOP;

    public boolean isStart() {
        return this.equals(ChannelStatus.START);
    }

    public boolean isPause() {
        return this.equals(ChannelStatus.PAUSE);
    }

    public boolean isStop() {
        return this.equals(ChannelStatus.STOP);
    }
}
