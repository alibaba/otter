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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.otter.manager.biz.common.alarm.AlarmMessage;
import com.alibaba.otter.manager.biz.monitor.AlarmController;
import com.alibaba.otter.manager.biz.monitor.AlarmRecovery;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.google.common.collect.OtterMigrateMap;

/**
 * @author zebin.xuzb 2012-9-11 下午3:47:28
 * @version 4.1.0
 */
public class DefaultAlarmController implements AlarmController {

    // seconds
    private Long                    DEFAULT_THRESHOLD = 1800L;
    private Map<PoolKey, PoolValue> pool              = OtterMigrateMap.makeSoftValueMapWithTimeout(1, TimeUnit.HOURS);
    private AlarmRecovery           restartAlarmRecovery;

    @Override
    public AlarmMessage control(AlarmRule rule, String message, AlarmMessage data) {
        // rule为空不控制
        if (rule == null) {
            return data;
        }

        // second
        Long threshold = rule.getIntervalTime() == null ? DEFAULT_THRESHOLD : rule.getIntervalTime();

        PoolKey key = new PoolKey(rule, message, data);
        PoolValue value = pool.get(key);
        boolean needAlarm = true;

        Long now = System.currentTimeMillis();
        // 第一次报警,或是之前已经清空了
        if (value == null) {
            value = new PoolValue(now);
            pool.put(key, new PoolValue(now));
        } else {
            Long latest = value.getLastAlarmTime();
            // 如果第二次报警超过阀值，则进行报警
            if ((now - latest) > (threshold * 1000)) {
                value.updateAlarmTime(now);// 更新最后一次报警时间
                pool.put(key, value);
            } else { // 第二次报警没有超过阀值，则存下来，不进行报警
                value.addSuppressTimes(); // 增加报警压制次数
                pool.put(key, value);
                needAlarm = false;
            }
        }

        if (rule.getAutoRecovery()) {// 尝试一下恢复机制
            restartAlarmRecovery.recovery(rule, value.getSuppressTimes());
        }

        if (needAlarm) {
            return data;
        } else {
            return null;
        }
    }

    private static class PoolKey {

        private Long        pipelineId;
        private MonitorName monitorName;
        private String      receiveKey;
        private String      matchValue;

        public PoolKey(AlarmRule rule, String messageToSend, AlarmMessage data){
            // used to hash compute
            this.pipelineId = rule.getPipelineId();
            this.monitorName = rule.getMonitorName();
            this.receiveKey = rule.getReceiverKey();
            this.matchValue = rule.getMatchValue();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((matchValue == null) ? 0 : matchValue.hashCode());
            result = prime * result + ((monitorName == null) ? 0 : monitorName.hashCode());
            result = prime * result + ((pipelineId == null) ? 0 : pipelineId.hashCode());
            result = prime * result + ((receiveKey == null) ? 0 : receiveKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            PoolKey other = (PoolKey) obj;
            if (matchValue == null) {
                if (other.matchValue != null) return false;
            } else if (!matchValue.equals(other.matchValue)) return false;
            if (monitorName != other.monitorName) return false;
            if (pipelineId == null) {
                if (other.pipelineId != null) return false;
            } else if (!pipelineId.equals(other.pipelineId)) return false;
            if (receiveKey == null) {
                if (other.receiveKey != null) return false;
            } else if (!receiveKey.equals(other.receiveKey)) return false;
            return true;
        }

    }

    private static class PoolValue {

        private Long lastAlarmTime;    // mill
        private long suppressTimes = 1; // 报警压制次数

        public PoolValue(Long happendTime){
            this.lastAlarmTime = happendTime;
        }

        /**
         * 增加报警压制次数
         */
        public void addSuppressTimes() {
            suppressTimes++;
        }

        /**
         * 获取报警压制次数
         */
        public long getSuppressTimes() {
            return suppressTimes;
        }

        /**
         * 最后报警时间
         */
        public Long getLastAlarmTime() {
            return lastAlarmTime;
        }

        /**
         * 更新报警时间
         */
        public void updateAlarmTime(Long lastAlarmTime) {
            this.lastAlarmTime = lastAlarmTime;
        }

    }

    public void setRestartAlarmRecovery(AlarmRecovery restartAlarmRecovery) {
        this.restartAlarmRecovery = restartAlarmRecovery;
    }

}
