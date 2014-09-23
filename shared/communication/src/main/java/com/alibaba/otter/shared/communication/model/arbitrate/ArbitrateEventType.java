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

package com.alibaba.otter.shared.communication.model.arbitrate;

import com.alibaba.otter.shared.communication.core.model.EventType;

public enum ArbitrateEventType implements EventType {

    /** 通知manager关闭 */
    stopChannel,
    /** 报警信息 */
    nodeAlarm,
    /** 通知manager node需要关闭 */
    stopNode,
    /** stage调度通知 */
    stageSingle,
    /** fast stage调度通知 */
    fastStageSingle;
}
