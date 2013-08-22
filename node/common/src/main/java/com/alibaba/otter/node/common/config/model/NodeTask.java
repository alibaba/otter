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

package com.alibaba.otter.node.common.config.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * S.E.T.L阶段模块的任务
 * 
 * @author jianghang
 */
public class NodeTask implements Serializable {

    private static final long serialVersionUID = 5442938515474956890L;
    private Pipeline          pipeline;
    private List<StageType>   stage            = new ArrayList<StageType>(); // 任务类型
    private List<TaskEvent>   event            = new ArrayList<TaskEvent>(); // 任务事件，新增/修改
    private boolean           shutdown         = false;

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    /**
     * 任务事件，新增/删除/修改
     */
    public static enum TaskEvent {
        CREATE, DELETE;

        public boolean isCreate() {
            return this.equals(TaskEvent.CREATE);
        }

        public boolean isDelete() {
            return this.equals(TaskEvent.DELETE);
        }

    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public List<StageType> getStage() {
        return stage;
    }

    public void setStage(List<StageType> stage) {
        this.stage = stage;
    }

    public List<TaskEvent> getEvent() {
        return event;
    }

    public void setEvent(List<TaskEvent> event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
