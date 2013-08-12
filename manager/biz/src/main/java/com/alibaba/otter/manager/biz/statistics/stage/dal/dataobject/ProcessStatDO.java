
package com.alibaba.otter.manager.biz.statistics.stage.dal.dataobject;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.model.statistics.stage.StageStat;
import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * TODO Comment of TableStat
 * 
 * @author danping.yudp
 */

public class ProcessStatDO implements Serializable{
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

