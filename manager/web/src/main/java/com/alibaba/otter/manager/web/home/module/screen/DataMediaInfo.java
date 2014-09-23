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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.web.common.model.SeniorDataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;

public class DataMediaInfo {

    @Resource(name = "dataMediaService")
    private DataMediaService     dataMediaService;

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService dataMediaPairService;

    @Resource(name = "channelService")
    private ChannelService       channelService;

    public void execute(@Param("dataMediaId") Long dataMediaId, Context context) throws Exception {
        DataMedia dataMedia = dataMediaService.findById(dataMediaId);

        List<DataMediaPair> dataMediaPairs = dataMediaPairService.listByDataMediaId(dataMediaId);

        List<SeniorDataMediaPair> seniorDataMediapairs = new ArrayList<SeniorDataMediaPair>();
        for (DataMediaPair dataMediaPair : dataMediaPairs) {
            SeniorDataMediaPair seniorDataMediaPair = new SeniorDataMediaPair();
            seniorDataMediaPair.setChannel(channelService.findByPipelineId(dataMediaPair.getPipelineId()));
            seniorDataMediaPair.setDataMediaPair(dataMediaPair);
            seniorDataMediapairs.add(seniorDataMediaPair);
        }

        context.put("dataMedia", dataMedia);
        context.put("seniorDataMediapairs", seniorDataMediapairs);
    }
}
