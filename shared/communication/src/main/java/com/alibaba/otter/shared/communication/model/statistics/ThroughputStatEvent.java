package com.alibaba.otter.shared.communication.model.statistics;

import java.util.List;

import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 吞吐量事件
 * 
 * @author jianghang
 */
public class ThroughputStatEvent extends Event {

    private static final long serialVersionUID = 3626191138534384067L;

    public ThroughputStatEvent(){
        super(StatisticsEventType.throughputStat);
    }

    private List<ThroughputStat> stats;

    public List<ThroughputStat> getStats() {
        return stats;
    }

    public void setStats(List<ThroughputStat> stats) {
        this.stats = stats;
    }

}
