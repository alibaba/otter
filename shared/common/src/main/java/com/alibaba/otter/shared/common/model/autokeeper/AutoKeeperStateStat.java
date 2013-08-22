/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.shared.common.model.autokeeper;

/**
 * 基本统计信息
 * 
 * @author jianghang 2012-9-21 下午02:12:03
 * @version 4.1.0
 */
public class AutoKeeperStateStat extends AutoKeeperStat {

    private static final long serialVersionUID = 250466030211280762L;
    private long              minLatency;
    private long              maxLatency;
    private long              avgLatency;
    private long              queued;                                // 等待队列
    private long              recved;                                // 接受队列
    private long              sent;                                  // 发送队列

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
