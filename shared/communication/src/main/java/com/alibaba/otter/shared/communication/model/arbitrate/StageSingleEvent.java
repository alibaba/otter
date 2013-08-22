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

import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.communication.core.model.EventType;

/**
 * 基于rpc实现仲裁器调度的通知信号对象
 * 
 * @author jianghang 2012-9-28 下午10:21:03
 * @version 4.1.0
 */
public class StageSingleEvent extends Event {

    private static final long serialVersionUID = 476657754177940448L;

    private Long              pipelineId;
    private StageType         stage;
    // 对应的对象类型为EtlEventData，因为依赖关系的问题，不能直接使用具体类,由序列化方式保证可以拿到具体的子类
    private Object            data;

    public StageSingleEvent(EventType type){
        super(type);
    }

    public StageType getStage() {
        return stage;
    }

    public void setStage(StageType stage) {
        this.stage = stage;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

}
