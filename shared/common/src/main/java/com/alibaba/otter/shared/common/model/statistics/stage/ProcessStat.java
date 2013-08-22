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

package com.alibaba.otter.shared.common.model.statistics.stage;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * @author jianghang 2011-9-8 下午12:43:53
 */
public class ProcessStat implements Serializable {

    private static final long serialVersionUID = -5625269232233751756L;
    private Long              pipelineId;
    private Long              processId;
    private List<StageStat>   stageStats;                              // 当前process的阶段列表

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public List<StageStat> getStageStats() {
        return stageStats;
    }

    public void setStageStats(List<StageStat> stageStats) {
        this.stageStats = stageStats;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
