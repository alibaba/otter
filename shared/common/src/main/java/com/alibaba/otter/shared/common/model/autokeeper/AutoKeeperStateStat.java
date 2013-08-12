package com.alibaba.otter.shared.common.model.autokeeper;

/**
 * 基本统计信息
 * 
 * @author jianghang 2012-9-21 下午02:12:03
 * @version 4.1.0
 */
public class AutoKeeperStateStat extends AutoKeeperStat {

    private long minLatency;
    private long maxLatency;
    private long avgLatency;
    private long queued;    // 等待队列
    private long recved;    // 接受队列
    private long sent;      // 发送队列

    public long getMinLatency() {
        return minLatency;
    }

    public void setMinLatency(long minLatency) {
        this.minLatency = minLatency;
    }

    public long getMaxLatency() {
        return maxLatency;
    }

    public void setMaxLatency(long maxLatency) {
        this.maxLatency = maxLatency;
    }

    public long getAvgLatency() {
        return avgLatency;
    }

    public void setAvgLatency(long avgLatency) {
        this.avgLatency = avgLatency;
    }

    public long getQueued() {
        return queued;
    }

    public void setQueued(long queued) {
        this.queued = queued;
    }

    public long getRecved() {
        return recved;
    }

    public void setRecved(long recved) {
        this.recved = recved;
    }

    public long getSent() {
        return sent;
    }

    public void setSent(long sent) {
        this.sent = sent;
    }

}
