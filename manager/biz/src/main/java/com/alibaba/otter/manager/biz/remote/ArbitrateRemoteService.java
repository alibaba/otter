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

package com.alibaba.otter.manager.biz.remote;

import com.alibaba.otter.shared.communication.model.arbitrate.NodeAlarmEvent;
import com.alibaba.otter.shared.communication.model.arbitrate.StopChannelEvent;
import com.alibaba.otter.shared.communication.model.arbitrate.StopNodeEvent;

public interface ArbitrateRemoteService {

    /**
     * 处理node信息报警
     */
    public void onNodeAlarm(NodeAlarmEvent event);

    /**
     * 处理客户端关闭channel的事件
     */
    public void onStopNode(StopNodeEvent event);

    /**
     * 处理客户端关闭channel的事件
     */
    public void onStopChannel(StopChannelEvent event);
}
