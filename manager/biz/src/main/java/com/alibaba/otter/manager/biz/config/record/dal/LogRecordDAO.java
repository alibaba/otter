package com.alibaba.otter.manager.biz.config.record.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO;

/**
 * 类LogRecordDao.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-6-15 下午1:50:01
 */
public interface LogRecordDAO extends GenericDAO<LogRecordDO> {

    public List<LogRecordDO> listByPipelineId(Long pipelineId);

    public List<LogRecordDO> listByPipelineIdWithoutContent(Long pipelineId);
}
