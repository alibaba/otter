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

package com.alibaba.otter.manager.web.home.module.screen.api;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.statistics.delay.DelayStatService;
import com.alibaba.otter.manager.biz.statistics.delay.param.TopDelayStat;
import com.alibaba.otter.manager.biz.statistics.delay.param.TopDelayStat.DataStat;
import com.alibaba.otter.manager.biz.statistics.throughput.ThroughputStatService;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputType;
import com.alibaba.otter.shared.common.utils.JsonUtils;

public class TopDelay extends AbstractJsonScreen<String> {

    @Resource(name = "throughputStatService")
    private ThroughputStatService throughputStatService;

    @Resource(name = "delayStatService")
    private DelayStatService      delayStatService;

    public void execute(@Param("searchKey") String searchKey, @Param("topN") int topN, @Param("statTime") int minute) {
        try {
            if (topN <= 0) {
                topN = 10;
            }

            if (minute <= 0) {
                minute = 1;
            }

            List<TopDelayStat> tops = delayStatService.listTopDelayStat(searchKey, topN);

            List<Long> pipelineIds = new ArrayList<Long>();
            for (TopDelayStat top : tops) {
                top.setStatTime(Long.valueOf(minute));
                pipelineIds.add(top.getPipelineId());
            }

            List<ThroughputStat> stats = throughputStatService.listRealtimeThroughputByPipelineIds(pipelineIds, minute);
            for (ThroughputStat stat : stats) {
                for (TopDelayStat top : tops) {
                    if (stat.getPipelineId().equals(top.getPipelineId())) {
                        DataStat s = new DataStat(stat.getNumber(), stat.getSize());
                        if (ThroughputType.FILE == stat.getType()) {
                            top.setFileStat(s);
                        } else if (ThroughputType.ROW == stat.getType()) {
                            top.setDbStat(s);
                        }
                        break;
                    }
                }
            }

            returnSuccess(JsonUtils.marshalToString(tops));
        } catch (Exception e) {
            String errorMsg = String.format("error happens while searchKey[%s] topN [%d]", searchKey, topN);
            log.error(errorMsg, e);
            returnError(errorMsg);
        }
    }
}
