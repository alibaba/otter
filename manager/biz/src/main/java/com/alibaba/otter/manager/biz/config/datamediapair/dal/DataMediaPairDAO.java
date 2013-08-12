package com.alibaba.otter.manager.biz.config.datamediapair.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO;

/**
 * @author simon
 */
public interface DataMediaPairDAO extends GenericDAO<DataMediaPairDO> {

    public List<DataMediaPairDO> listByPipelineId(Long pipelineId);

    public List<DataMediaPairDO> listByDataMediaId(Long dataMediaId);

}
