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

package com.alibaba.otter.node.common.statistics;

import java.util.List;

import com.alibaba.otter.shared.common.model.statistics.delay.DelayCount;
import com.alibaba.otter.shared.common.model.statistics.table.TableStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;

/**
 * 统计信息的本地service
 * 
 * @author jianghang
 */
public interface StatisticsClientService {

    /**
     * 发送增加 delay queue的统计数据
     */
    public void sendIncDelayCount(DelayCount delayCount);

    /**
     * 发送减少 delay queue的统计数据
     */
    public void sendDecDelayCount(DelayCount delayCount);

    /**
     * 发送reset delay queue的统计数据
     */
    public void sendResetDelayCount(DelayCount delayCount);

    /**
     * 发送吞吐量相关统计信息
     */
    public void sendThroughputs(List<ThroughputStat> stats);

    /**
     * 发送table load相关数据信息
     */
    public void sendTableStats(List<TableStat> stats);

}
