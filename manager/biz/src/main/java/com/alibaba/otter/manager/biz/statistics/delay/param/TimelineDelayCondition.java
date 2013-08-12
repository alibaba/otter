package com.alibaba.otter.manager.biz.statistics.delay.param;

import java.util.Date;

/**
 * @author danping.yudp
 */
public class TimelineDelayCondition {

    private Long pipelineId;
    private Date start;
    private Date end;

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}
