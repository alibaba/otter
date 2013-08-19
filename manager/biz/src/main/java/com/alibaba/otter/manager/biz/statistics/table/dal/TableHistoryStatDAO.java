package com.alibaba.otter.manager.biz.statistics.table.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableHistoryStatDO;
import com.alibaba.otter.manager.biz.statistics.table.param.BehaviorHistoryCondition;

public interface TableHistoryStatDAO {

    /**
     * 插入记录
     */
    public void insertTableHistoryStat(TableHistoryStatDO tableHistoryStatDO);

    /**
     * 根据pairId列出 start-end时间段下的tableStat
     */
    public List<TableHistoryStatDO> listTimelineTableStat(BehaviorHistoryCondition condition);
}
