package com.alibaba.otter.manager.biz.statistics.stage.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.statistics.stage.dal.dataobject.ProcessStatDO;

/**
 * TODO Comment of ProcessDAO
 * 
 * @author danping.yudp
 */
public interface ProcessDAO {
    public void insertProcessStat(ProcessStatDO processStat);

    public void deleteProcessStat(Long processId);

    public void modifyProcessStat(ProcessStatDO processStat);

    public ProcessStatDO findByProcessId(Long processId);

    public List<ProcessStatDO> listAllProcessStat();

    public List<ProcessStatDO> listProcessStatsByPipelineId(Long pipelineId);

}
