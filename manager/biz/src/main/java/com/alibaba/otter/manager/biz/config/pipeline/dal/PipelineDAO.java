package com.alibaba.otter.manager.biz.config.pipeline.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO;

/**
 * @author simon
 */
public interface PipelineDAO extends GenericDAO<PipelineDO> {

    public List<PipelineDO> listByChannelIds(Long... channelId);
}
