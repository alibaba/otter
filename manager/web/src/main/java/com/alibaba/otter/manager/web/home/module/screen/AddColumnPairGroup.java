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

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.datacolumnpair.DataColumnPairGroupService;
import com.alibaba.otter.manager.biz.config.datacolumnpair.DataColumnPairService;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.shared.common.model.config.data.ColumnGroup;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;

public class AddColumnPairGroup {

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService       dataMediaPairService;

    @Resource(name = "dataMediaService")
    private DataMediaService           dataMediaService;

    @Resource(name = "dataColumnPairGroupService")
    private DataColumnPairGroupService dataColumnPairGroupService;

    @Resource(name = "dataColumnPairService")
    private DataColumnPairService      dataColumnPairService;

    public void execute(@Param("dataMediaPairId") Long dataMediaPairId, @Param("pipelineId") Long pipelineId,
                        @Param("channelId") Long channelId, @Param("sourceMediaId") Long sourceMediaId,
                        @Param("targetMediaId") Long targetMediaId, Context context) throws Exception {
        List<ColumnPair> columnPairs = dataColumnPairService.listByDataMediaPairId(dataMediaPairId);
        if (CollectionUtils.isEmpty(columnPairs)) {
            columnPairs.addAll(buildColumnPairFromDataMedia(dataMediaPairId, sourceMediaId, targetMediaId));
        } else {
            DataMediaPair dataMediaPair = dataMediaPairService.findById(dataMediaPairId);
            if (dataMediaPair.getColumnPairMode().isExclude()) {
                List<ColumnPair> allColumnPairs = buildColumnPairFromDataMedia(dataMediaPairId, sourceMediaId,
                                                                               targetMediaId);
                allColumnPairs.removeAll(columnPairs); // 如果是exclude模式，排除掉
                columnPairs = allColumnPairs;
            }
        }

        List<ColumnGroup> columnGroups = dataColumnPairGroupService.listByDataMediaPairId(dataMediaPairId);
        List<ColumnPair> columnPairGroup = new ArrayList<ColumnPair>();

        if (CollectionUtils.isNotEmpty(columnGroups)) {
            for (ColumnGroup columnGroup : columnGroups) {
                List<ColumnPair> columnPairTemp = new ArrayList<ColumnPair>();
                columnPairGroup = columnGroup.getColumnPairs();
                for (ColumnPair columnPair : columnPairGroup) {
                    for (ColumnPair subColumnPair : columnPairs) {
                        if (columnPair.equals(subColumnPair)) {
                            columnPairTemp.add(subColumnPair);
                        }
                    }
                }
                // 如果是多个Group，需要深度复制columnPair,每个Group需要复制一个
                columnPairs.removeAll(columnPairTemp);
            }
        }

        context.put("preColumnPairs", columnPairs); // 未加入group的columnPair定义
        context.put("columnPairs", columnPairGroup);// 当前group的columnPair定义
        context.put("dataMediaPairId", dataMediaPairId);
        context.put("channelId", channelId);
        context.put("pipelineId", pipelineId);

    }

    private List<ColumnPair> buildColumnPairFromDataMedia(Long dataMediaPairId, Long sourceMediaId, Long targetMediaId) {
        List<ColumnPair> columnPairs = new ArrayList<ColumnPair>();
        List<String> sourceColumns = dataMediaService.queryColumnByMediaId(sourceMediaId);
        List<String> targetColumns = dataMediaService.queryColumnByMediaId(targetMediaId);

        if (CollectionUtils.isNotEmpty(sourceColumns) && CollectionUtils.isNotEmpty(targetColumns)) {
            for (String sourceColumn : sourceColumns) {
                for (String targetColumn : targetColumns) {
                    if (sourceColumn.equalsIgnoreCase(targetColumn)) {
                        ColumnPair temp = new ColumnPair(sourceColumn, targetColumn);
                        temp.setDataMediaPairId(dataMediaPairId);
                        columnPairs.add(temp);
                    }
                }
            }
        }

        return columnPairs;
    }
}
