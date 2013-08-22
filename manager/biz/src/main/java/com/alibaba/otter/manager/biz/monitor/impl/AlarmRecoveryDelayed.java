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

package com.alibaba.otter.manager.biz.monitor.impl;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.alibaba.otter.manager.biz.common.arbitrate.DeadNodeListener.DeadNodeDelayed;

/**
 * recovery异步延迟处理对象
 * 
 * @author jianghang 2012-9-20 上午10:42:35
 * @version 4.1.0
 */
public class AlarmRecoveryDelayed implements Delayed {

    // init time for nano
    private static final long MILL_ORIGIN = System.currentTimeMillis();
    private long              ruleId;
    private long              channelId;
    private boolean           stop        = false;
    private long              now;                                     // 记录具体的now的偏移时间点，单位ms
    private long              timeout;                                 // 记录具体需要被delayed处理的偏移时间点,单位ms

    public AlarmRecoveryDelayed(long channelId, long ruleId, boolean stop, long timeout){
        this.channelId = channelId;
        this.ruleId = ruleId;
        this.stop = stop;
        this.timeout = timeout;
        this.now = System.currentTimeMillis() - MILL_ORIGIN;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getRuleId() {
        return ruleId;
    }

    public long getNow() {
        return now;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public long getDelay(TimeUnit unit) {
        long currNow = System.currentTimeMillis() - MILL_ORIGIN;
        long d = unit.convert(now + timeout - currNow, TimeUnit.MILLISECONDS);
        return d;
    }

    public int compareTo(Delayed other) {
        if (other == this) { // compare zero ONLY if same object
            return 0;
        } else if (other instanceof AlarmRecoveryDelayed) {
            AlarmRecoveryDelayed x = (AlarmRecoveryDelayed) other;
            long diff = now + timeout - (x.now + x.timeout);
            return (diff == 0) ? 0 : ((diff < 0) ? 1 : -1); // 时间越小的，越应该排在前面
        } else {
            long d = (getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS));
            return (d == 0) ? 0 : ((d < 0) ? 1 : -1);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (channelId ^ (channelId >>> 32));
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DeadNodeDelayed)) {
            return false;
        }
        AlarmRecoveryDelayed other = (AlarmRecoveryDelayed) obj;
        if (channelId != other.channelId) {
            return false;
        }
        return true;
    }

}
