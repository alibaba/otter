package com.alibaba.otter.manager.biz.statistics.table.param;

import java.util.Date;

/**
 * @author sarah.lij 2012-7-13 下午04:51:55
 */
public class TimelineBehaviorHistoryCondition extends BehaviorHistoryCondition {

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
