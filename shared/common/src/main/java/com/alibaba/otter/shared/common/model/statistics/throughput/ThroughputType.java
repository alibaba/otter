package com.alibaba.otter.shared.common.model.statistics.throughput;

/**
 * 吞吐量统计类型
 * 
 * @author jianghang 2011-9-8 下午12:57:30
 */
public enum ThroughputType {
    /** 数据库行记录 */
    ROW,
    /** 文件数据 */
    FILE,
    /** MQ记录数据 */
    MQ;
}
