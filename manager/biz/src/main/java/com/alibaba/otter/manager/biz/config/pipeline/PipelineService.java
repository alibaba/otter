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

    public List<Pipeline> listByDestinationWithoutOther(String destination);
}
