package com.alibaba.otter.manager.biz.statistics.stage.dal.ibatis;

import java.util.List;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.manager.biz.statistics.stage.dal.ProcessDAO;
import com.alibaba.otter.manager.biz.statistics.stage.dal.dataobject.ProcessStatDO;

/**
 * TODO Comment of IbatisProcessStatDAO
 * 
 * @author danping.yudp
 */
public class IbatisProcessDAO extends SqlMapClientDaoSupport implements ProcessDAO {

    @Override
    public void insertProcessStat(ProcessStatDO processStat) {

        getSqlMapClientTemplate().insert("", processStat);
    }

    @Override
    public void deleteProcessStat(Long processId) {
        getSqlMapClientTemplate().delete("", processId);
    }

    @Override
    public void modifyProcessStat(ProcessStatDO processStat) {
        getSqlMapClientTemplate().update("", processStat);
    }

    @Override
    public ProcessStatDO findByProcessId(Long processId) {

        return (ProcessStatDO) getSqlMapClientTemplate().queryForObject("", processId);
    }

    @Override
    public List<ProcessStatDO> listAllProcessStat() {

        return (List<ProcessStatDO>) getSqlMapClientTemplate().queryForList("");
    }

    @Override
    public List<ProcessStatDO> listProcessStatsByPipelineId(Long pipelineId) {

        return (List<ProcessStatDO>) getSqlMapClientTemplate().queryForList("", pipelineId);
    }

}
