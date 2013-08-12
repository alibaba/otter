package com.alibaba.otter.manager.biz.config.channel;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.manager.biz.common.baseservice.GenericService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;

/**
 * @author simon
 */
public interface ChannelService extends GenericService<Channel> {

    public Channel findByPipelineId(Long pipelineId);

    public Channel findByIdWithoutColumn(Long pipelineId);

    public List<Channel> listByPipelineIds(Long... pipelineIds);

    public List<Channel> listByNodeId(Long nodeId);

    public List<Channel> listOnlyChannels(Long... identities);

    public List<Long> listAllChannelId();

    public List<Channel> listByNodeId(Long nodeId, ChannelStatus... status);

    public List<Channel> listByConditionWithoutColumn(Map condition);

    public void stopChannel(Long channelId);

    public void notifyChannel(Long channelId);

    public void startChannel(Long channelId);

}
