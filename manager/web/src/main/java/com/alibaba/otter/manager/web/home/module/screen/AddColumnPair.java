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
import com.alibaba.otter.manager.biz.config.datacolumnpair.DataColumnPairService;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;

public class AddColumnPair {

    @Resource(name = "dataMediaService")
    private DataMediaService      dataMediaService;

    @Resource(name = "dataColumnPairService")
    private DataColumnPairService dataColumnPairService;

    public void execute(@Param("dataMediaPairId") Long dataMediaPairId, @Param("channelId") Long channelId,
                        @Param("pipelineId") Long pipelineId, @Param("sourceMediaId") Long sourceMediaId,
                        @Param("targetMediaId") Long targetMediaId, Context context) throws Exception {
        @SuppressWarnings("unchecked")
        DataMedia sourcedataMedia = dataMediaService.findById(sourceMediaId);
        DataMedia targetdataMedia = dataMediaService.findById(targetMediaId);

        List<String> sourceColumns = dataMediaService.queryColumnByMedia(sourcedataMedia);
        List<String> targetColumns = dataMediaService.queryColumnByMedia(targetdataMedia);

        List<String> underSourceColumns = new ArrayList<String>();
        List<String> underTargetColumns = new ArrayList<String>();

        List<ColumnPair> columnPairs = dataColumnPairService.listByDataMediaPairId(dataMediaPairId);

        if (columnPairs != null && !columnPairs.isEmpty()) {
            for (ColumnPair columnPair : columnPairs) {
                if (columnPair.getSourceColumn() != null) {
                    underSourceColumns.add(columnPair.getSourceColumn().getName());
                }
                if (columnPair.getTargetColumn() != null) {
                    underTargetColumns.add(columnPair.getTargetColumn().getName());
                }
            }
        }

        sourceColumns.removeAll(underSourceColumns);
        targetColumns.removeAll(underTargetColumns);

        context.put("sourceMediaId", sourceMediaId);
        context.put("targetMediaId", targetMediaId);
        context.put("sourceColumns", sourceColumns);
        context.put("targetColumns", targetColumns);
        context.put("underSourceColumns", underSourceColumns);
        context.put("underTargetColumns", underTargetColumns);
        context.put("dataMediaPairId", dataMediaPairId);
        context.put("channelId", channelId);
        context.put("pipelineId", pipelineId);
    }

}
