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

package com.alibaba.otter.manager.biz.statistics.throughput;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.manager.biz.statistics.throughput.param.AnalysisType;
import com.alibaba.otter.manager.biz.statistics.throughput.param.RealtimeThroughputCondition;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputCondition;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputInfo;
import com.alibaba.otter.manager.biz.statistics.throughput.param.TimelineThroughputCondition;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;

/**
 * @author jianghang 2011-9-8 下午01:26:31
 */
public interface ThroughputStatService {

    public Map<AnalysisType, ThroughputInfo> listRealtimeThroughput(RealtimeThroughputCondition condition);

    public Map<Long, ThroughputInfo> listTimelineThroughput(TimelineThroughputCondition condition);

    public List<ThroughputStat> listRealtimeThroughputByPipelineIds(List<Long> pipelineIds, int minute);

    public ThroughputStat findThroughputStatByPipelineId(ThroughputCondition condition);

    public void createOrUpdateThroughput(ThroughputStat item);
}
