package com.alibaba.otter.manager.biz.statistics.table.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableStatDO;

/**
 * @author simon
 */
public interface TableStatDAO {

    public void insertTableStat(TableStatDO tableStat);

    public void deleteTableStat(Long tableStatId);

    public int modifyTableStat(TableStatDO tableStat);

    public TableStatDO findTableStatById(Long tableStatId);

    public TableStatDO findTableStatByPipelineIdAndPairId(Long pipelineId, Long dataMediaPairId);

    public List<TableStatDO> listTableStatsByPipelineId(Long pipelineId);

    public List<TableStatDO> listTableStatsByPairId(Long dataMediaPairId);

}
