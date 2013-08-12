package com.alibaba.otter.manager.biz.config.pipeline;

import java.util.List;

import com.alibaba.otter.manager.biz.common.baseservice.GenericService;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * @author simon
 */
public interface PipelineService extends GenericService<Pipeline> {

    public List<Pipeline> listByChannelIds(Long... channelIds);

    public List<Pipeline> listByChannelIdsWithoutOther(Long... channelIds);

    public List<Pipeline> listByChannelIdsWithoutColumn(Long... channelIds);

    public List<Pipeline> listByNodeId(Long nodeId);

    public boolean hasRelation(Long nodeId);
}
