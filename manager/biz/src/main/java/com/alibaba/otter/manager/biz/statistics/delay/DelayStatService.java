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

package com.alibaba.otter.manager.biz.statistics.delay;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.otter.manager.biz.statistics.delay.param.DelayStatInfo;
import com.alibaba.otter.manager.biz.statistics.delay.param.TopDelayStat;
import com.alibaba.otter.shared.common.model.statistics.delay.DelayStat;

/**
 * @author jianghang 2011-9-8 下午12:37:14
 */
public interface DelayStatService {

    public void createDelayStat(DelayStat stat);

    public DelayStat findRealtimeDelayStat(Long pipelineId);

    public Map<Long, DelayStatInfo> listTimelineDelayStat(Long pipelineId, Date start, Date end);

    public List<TopDelayStat> listTopDelayStat(String searchKey, int topN);

}
