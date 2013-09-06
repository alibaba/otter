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

package com.alibaba.otter.shared.common.model.config.alarm;

/**
 * @author simon 2012-8-29 下午7:33:53
 * @version 4.1.0
 */
public enum MonitorName {

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
