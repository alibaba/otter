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

package com.alibaba.otter.manager.web.home.module.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.statistics.table.TableStat;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.biz.statistics.table.TableStatService;

public class DataMediaPairList {

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService dataMediaPairService;

    @Resource(name = "channelService")
    private ChannelService       channelService;

    @Resource(name = "tableStatService")
    private TableStatService     tableStatService;

    public void execute(@Param("pipelineId") Long pipelineId, Context context) throws Exception {
        // Pipeline pipeline = pipelineService.findById(pipelineId);
        Channel channel = channelService.findByPipelineId(pipelineId);
        List<DataMediaPair> dataMediaPairs = dataMediaPairService.listByPipelineId(pipelineId);
        Map<Long, TableStat> tableStatMap = new HashMap<Long, TableStat>(dataMediaPairs.size(), 1f);
        List<TableStat> tableStats = tableStatService.listTableStat(pipelineId);

        for (DataMediaPair dataMediaPair : dataMediaPairs) {
            int flag = 0;
            for (TableStat tableStat : tableStats) {
                if (dataMediaPair.getId().equals(tableStat.getDataMediaPairId())) {
                    tableStatMap.put(dataMediaPair.getId(), tableStat);
                    flag = 1;
                    break;
                }
            }
            if (flag == 0) {
                TableStat tableStat = new TableStat();
                tableStat.setFileSize(0L);
                tableStat.setFileCount(0L);
                tableStat.setDeleteCount(0L);
                tableStat.setUpdateCount(0L);
                tableStat.setInsertCount(0L);
                // tableStat.setGmtModified(dataMediaPair.getGmtModified());
                tableStatMap.put(dataMediaPair.getId(), tableStat);
            }
        }

        context.put("dataMediaPairs", dataMediaPairs);

        // 通过PipelineId不能获取到Channel状态，所以需要传递Channel对象

        context.put("channel", channel);
        context.put("pipelineId", pipelineId);
        context.put("tableStatMap", tableStatMap);
    }
}
