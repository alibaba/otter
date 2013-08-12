package com.alibaba.otter.manager.biz.remote;

import com.alibaba.otter.shared.communication.model.statistics.DelayCountEvent;
import com.alibaba.otter.shared.communication.model.statistics.TableStatEvent;
import com.alibaba.otter.shared.communication.model.statistics.ThroughputStatEvent;

/**
 * 统计相关远程接口定义
 * 
 * @author jianghang
 */
public interface StatsRemoteService {

    /**
     * 接收inc delay统计信息
     */
    public void onDelayCount(DelayCountEvent event);

    /**
     * 接收table load相关数据信息
     */
    public void onTableStat(TableStatEvent event);

    /**
     * 接收吞吐量相关统计信息
     */
    public void onThroughputStat(ThroughputStatEvent event);

}
