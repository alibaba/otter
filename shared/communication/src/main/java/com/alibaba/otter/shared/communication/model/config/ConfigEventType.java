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

package com.alibaba.otter.shared.communication.model.config;

import com.alibaba.otter.shared.communication.core.model.EventType;

/**
 * config交互的事件类型
 * 
 * @author jianghang
 */
public enum ConfigEventType implements EventType {

    /** 查询nid对应的任务列表 */
    findTask,
    /** 根据nid查询Node对象 */
    findNode,
    /** 根据id查询对应的channel对象 */
    findChannel,
    /** manager通知task channel的变化 */
    notifyChannel,
    /** 查询media信息 */
    findMedia,
    /** 通知medai信息 */
    notifyMedia;

}
