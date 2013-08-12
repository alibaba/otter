package com.alibaba.otter.node.common.statistics;

import java.util.List;

import com.alibaba.otter.shared.common.model.statistics.delay.DelayCount;
import com.alibaba.otter.shared.common.model.statistics.table.TableStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;

/**
 * 统计信息的本地service
 * 
 * @author jianghang
 */
public interface StatisticsClientService {

    /**
     * 发送增加 delay queue的统计数据
     */
    public void sendIncDelayCount(DelayCount delayCount);

    /**
     * 发送减少 delay queue的统计数据
     */
    public void sendDecDelayCount(DelayCount delayCount);

    /**
     * 发送reset delay queue的统计数据
     */
    public void sendResetDelayCount(DelayCount delayCount);

    /**
     * 发送吞吐量相关统计信息
     */
    public void sendThroughputs(List<ThroughputStat> stats);

    /**
     * 发送table load相关数据信息
     */
    public void sendTableStats(List<TableStat> stats);

}
