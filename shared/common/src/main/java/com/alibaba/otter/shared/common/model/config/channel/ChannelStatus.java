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
