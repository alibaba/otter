/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
