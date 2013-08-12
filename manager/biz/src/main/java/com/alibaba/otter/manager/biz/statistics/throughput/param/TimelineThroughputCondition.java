package com.alibaba.otter.manager.biz.statistics.throughput.param;

import java.util.Date;

/**
 * @author jianghang 2011-9-8 下午01:25:25
 */
public class TimelineThroughputCondition extends ThroughputCondition {

    private Date start;
    private Date end;

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
