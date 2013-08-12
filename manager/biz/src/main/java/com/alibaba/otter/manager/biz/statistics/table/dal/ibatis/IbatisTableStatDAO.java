package com.alibaba.otter.manager.biz.statistics.table.dal.ibatis;

import java.util.List;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.manager.biz.statistics.table.dal.TableStatDAO;
import com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableStatDO;
import com.alibaba.otter.manager.biz.statistics.table.param.BehaviorHistoryCondition;

/**
 * @author simon
 */
public class IbatisTableStatDAO extends SqlMapClientDaoSupport implements TableStatDAO {

    @Override
    public void insertTableStat(TableStatDO tableStat) {

        getSqlMapClientTemplate().insert("insertTableStat", tableStat);
    }

    @Override
    public void deleteTableStat(Long tableStatId) {
        getSqlMapClientTemplate().delete("deleteTableStatById", tableStatId);
    }

    @Override
    public int modifyTableStat(TableStatDO tableStat) {
        return (getSqlMapClientTemplate().update("modifyTableStat", tableStat));
    }

    @Override
    public TableStatDO findTableStatById(Long tableStatId) {

        return (TableStatDO) getSqlMapClientTemplate().queryForObject("findTableStatById", tableStatId);
    }

    @Override
    public TableStatDO findTableStatByPipelineIdAndPairId(Long pipelineId, Long dataMediaPairId) {
        TableStatDO tableStat = new TableStatDO();
        tableStat.setPipelineId(pipelineId);
        tableStat.setDataMediaPairId(dataMediaPairId);
        return (TableStatDO) getSqlMapClientTemplate().queryForObject("findTableStatByPipelineIdAndDataMediaPairId",
                                                                      tableStat);
    }

    @Override
    public List<TableStatDO> listTableStatsByPipelineId(Long pipelineId) {

        return (List<TableStatDO>) getSqlMapClientTemplate().queryForList("listTableStatsByPipelineId", pipelineId);
    }

    @Override
    public List<TableStatDO> listTableStatsByPairId(Long dataMediaPairId) {

        return (List<TableStatDO>) getSqlMapClientTemplate().queryForList("listTableStatsByDataMediaPairId",
                                                                          dataMediaPairId);
    }

    public List<TableStatDO> listTimelineTableStat(BehaviorHistoryCondition condition) {
        return (List<TableStatDO>) getSqlMapClientTemplate().queryForList("listTimelineTableStat", condition);
    }

}
