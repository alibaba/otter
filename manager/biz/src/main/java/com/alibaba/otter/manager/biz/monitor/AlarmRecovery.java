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

package com.alibaba.otter.manager.biz.monitor;

import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;

/**
 * 报警尝试自行恢复机制
 * 
 * @author jianghang 2012-9-19 下午04:43:30
 * @version 4.1.0
 */
public interface AlarmRecovery {

    /**
     * 根据规则，强制进行recovery操作
     */
    public void recovery(Long channelId);

    /**
     * 根据规则，强制进行recovery操作
     */
    public void recovery(AlarmRule alarmRule);

    /**
     * 根据规则+触发次数，进行recovery操作
     */
    public void recovery(AlarmRule alarmRule, long alarmCount);
}
