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

package com.alibaba.otter.manager.biz.statistics.table;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.manager.biz.statistics.table.param.BehaviorHistoryInfo;
import com.alibaba.otter.manager.biz.statistics.table.param.TimelineBehaviorHistoryCondition;
import com.alibaba.otter.shared.common.model.statistics.table.TableStat;

/**
 * @author jianghang 2011-9-8 下午06:27:29
 */
public interface TableStatService {

    /**
     * 增量更新对应的Table统计状态
     */
    public void updateTableStat(TableStat stat);

    /**
     * 查询对应同步任务下的统计信息
     */
    public List<TableStat> listTableStat(Long pipelineId);

    /**
     * 更新对应的history信息，做报表统计用
     */
    public void insertBehaviorHistory(TableStat stat);

    /**
     * 查询对应的报表数据
     */
    public Map<Long, BehaviorHistoryInfo> listTimelineBehaviorHistory(TimelineBehaviorHistoryCondition condition);
}
