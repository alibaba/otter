package com.alibaba.otter.manager.biz.config.pipeline.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineNodeRelationDO;

/**
 * 考虑是否需要 类PipelineNodeRelationDAO.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-10-31 下午11:41:53
 */
public interface PipelineNodeRelationDAO extends GenericDAO<PipelineNodeRelationDO> {

    public void insertBatch(List<PipelineNodeRelationDO> pipelineNodeRelationDos);

    public void updateByNodeId(Long... nodeId);

    public void deleteByPipelineId(Long pipelineId);

    public void deleteByNodeId(Long... nodeId);

    public List<PipelineNodeRelationDO> listByPipelineIds(Long... pipelineId);

    public List<PipelineNodeRelationDO> listByNodeId(Long nodeId);
}
