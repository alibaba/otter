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

package com.alibaba.otter.manager.biz.statistics.delay.dal;

import java.util.Date;
import java.util.List;

import com.alibaba.otter.manager.biz.statistics.delay.dal.dataobject.DelayStatDO;
import com.alibaba.otter.manager.biz.statistics.delay.param.TopDelayStat;

/**
 * @author simon
 */
public interface DelayStatDAO {

    public void insertDelayStat(DelayStatDO delayStat);

    public void deleteDelayStat(Long delayStatId);

    public void modifyDelayStat(DelayStatDO delayStat);

    public DelayStatDO findDelayStatById(Long delayStatId);

    public DelayStatDO findRealtimeDelayStat(Long pipelineId);

    public List<DelayStatDO> listDelayStatsByPipelineId(Long pipelineId);

    public List<DelayStatDO> listTimelineDelayStatsByPipelineId(Long pipelineId, Date start, Date end);

    public List<TopDelayStat> listTopDelayStatsByName(String name, int topN);
}
