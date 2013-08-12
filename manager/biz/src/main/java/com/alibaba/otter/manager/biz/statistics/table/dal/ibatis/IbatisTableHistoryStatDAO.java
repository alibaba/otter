package com.alibaba.otter.manager.biz.statistics.table.dal.ibatis;

import java.util.List;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.manager.biz.statistics.table.dal.TableHistoryStatDAO;
import com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableHistoryStatDO;
import com.alibaba.otter.manager.biz.statistics.table.param.BehaviorHistoryCondition;

/**
 * @author sarah.lij 2012-7-17 下午06:35:52
 */
public class IbatisTableHistoryStatDAO extends SqlMapClientDaoSupport implements TableHistoryStatDAO {

    @Override
    public void insertTableHistoryStat(TableHistoryStatDO tableHistoryStatDO) {
        getSqlMapClientTemplate().insert("insertTableHistoryStat", tableHistoryStatDO);
    }

    @Override
    public List<TableHistoryStatDO> listTimelineTableStat(BehaviorHistoryCondition condition) {
        return (List<TableHistoryStatDO>) getSqlMapClientTemplate().queryForList("listTimelineTableStat", condition);
    }

}
