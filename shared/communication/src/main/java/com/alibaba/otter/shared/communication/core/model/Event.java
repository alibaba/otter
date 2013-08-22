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

package com.alibaba.otter.shared.communication.core.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * 通讯事件对象
 * 
 * @author jianghang 2011-9-9 下午04:02:53
 */
public abstract class Event implements Serializable {

    private static final long serialVersionUID = 208038167977229245L;

    private EventType         type;

    protected Event(){
    }

    protected Event(EventType type){
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
