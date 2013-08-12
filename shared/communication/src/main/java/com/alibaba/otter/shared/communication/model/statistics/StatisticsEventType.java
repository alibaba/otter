package com.alibaba.otter.shared.communication.model.statistics;

import com.alibaba.otter.shared.communication.core.model.EventType;

/**
 * 统计数据的事件类型
 * 
 * @author jianghang
 */
public enum StatisticsEventType implements EventType {
    /** delayCount */
    delayCount,
    /** tableStat */
    tableStat,
    /** throughputStat */
    throughputStat;
}
