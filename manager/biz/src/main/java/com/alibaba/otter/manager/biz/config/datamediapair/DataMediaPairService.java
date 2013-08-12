package com.alibaba.otter.manager.biz.config.datamediapair;

import java.util.List;

import com.alibaba.otter.manager.biz.common.baseservice.GenericService;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;

/**
 * @author simon
 */
public interface DataMediaPairService extends GenericService<DataMediaPair> {

    public List<DataMediaPair> listByPipelineId(Long pipelineId);

    public List<DataMediaPair> listByPipelineIdWithoutColumn(Long pipelineId);

    public List<DataMediaPair> listByDataMediaId(Long dataMediaId);

    public Long createAndReturnId(DataMediaPair dataMediaPair);

    public boolean createIfNotExist(DataMediaPair dataMediaPair);
}
