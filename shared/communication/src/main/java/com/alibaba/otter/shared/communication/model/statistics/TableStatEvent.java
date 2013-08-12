package com.alibaba.otter.shared.communication.model.statistics;

import java.util.List;

import com.alibaba.otter.shared.common.model.statistics.table.TableStat;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * table stat事件
 * 
 * @author jianghang
 */
public class TableStatEvent extends Event {

    private static final long serialVersionUID = -5925977847006864387L;

    public TableStatEvent(){
        super(StatisticsEventType.tableStat);

    }

    private List<TableStat> stats;

    public List<TableStat> getStats() {
        return stats;
    }

    public void setStats(List<TableStat> stats) {
        this.stats = stats;
    }

}
