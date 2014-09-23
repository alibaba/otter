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

package com.alibaba.otter.manager.biz.statistics.stage;

import java.util.Date;
import java.util.List;

import com.alibaba.otter.shared.common.model.statistics.stage.ProcessStat;

/**
 * @author jianghang 2011-9-8 下午12:48:21
 */
public interface ProcessStatService {

    public List<ProcessStat> listRealtimeProcessStat(Long pipelineId);

    public List<ProcessStat> listRealtimeProcessStat(Long channelId, Long pipelineId);

    public List<ProcessStat> listTimelineProcessStat(Long pipelineId, Date start, Date end);

    public void createProcessStat(ProcessStat stat);
}
