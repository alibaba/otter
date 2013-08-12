package com.alibaba.otter.shared.communication.model.statistics;

import com.alibaba.otter.shared.common.model.statistics.delay.DelayCount;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * delay queue事件
 * 
 * @author jianghang
 */
public class DelayCountEvent extends Event {

    private static final long serialVersionUID = -5925977847006864387L;

    public DelayCountEvent(){
        super(StatisticsEventType.delayCount);
    }

    public static enum Action {
        INC, DEC, RESET;

        public boolean isInc() {
            return this == INC;
        }

        public boolean isDec() {
            return this == DEC;
        }

        public boolean isReset() {
            return this == RESET;
        }
    }

    private DelayCount count;

    private Action     action;

    public DelayCount getCount() {
        return count;
    }

    public void setCount(DelayCount count) {
        this.count = count;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

}
