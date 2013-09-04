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

package com.alibaba.otter.node.etl.extract.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.etl.extract.exceptions.ExtractException;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;
import com.alibaba.otter.shared.common.model.config.data.ColumnPairMode;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;

/**
 * @author simon 2012-4-26 下午5:04:51
 */
public class ViewExtractor extends AbstractExtractor<DbBatch> {

    @Override
    public void extract(DbBatch dbBatch) throws ExtractException {
        Assert.notNull(dbBatch);
        Assert.notNull(dbBatch.getRowBatch());

        Pipeline pipeline = getPipeline(dbBatch.getRowBatch().getIdentity().getPipelineId());
        List<DataMediaPair> dataMediaPairs = pipeline.getPairs();

        /**
         * Key = TableId<br>
         * Value = a List of this tableId's column need to sync<br>
         */
        Map<Long, List<ColumnPair>> viewColumnPairs = new HashMap<Long, List<ColumnPair>>();
        Map<Long, ColumnPairMode> viewColumnPairModes = new HashMap<Long, ColumnPairMode>();

        for (DataMediaPair dataMediaPair : dataMediaPairs) {
            List<ColumnPair> columnPairs = dataMediaPair.getColumnPairs();
            // 设置ColumnPairMode
            viewColumnPairModes.put(dataMediaPair.getSource().getId(), dataMediaPair.getColumnPairMode());
            // 如果没有columnPairs，则默认全字段同步，不做处理
            if (!CollectionUtils.isEmpty(columnPairs)) {
                viewColumnPairs.put(dataMediaPair.getSource().getId(), columnPairs);
            }
        }

        List<EventData> eventDatas = dbBatch.getRowBatch().getDatas();
        Set<EventData> removeDatas = new HashSet<EventData>();// 使用set，提升remove时的查找速度
        for (EventData eventData : eventDatas) {
            if (eventData.getEventType().isDdl()) {
                continue;
            }

            List<ColumnPair> columns = viewColumnPairs.get(eventData.getTableId());
            if (!CollectionUtils.isEmpty(columns)) {
                // 组装需要同步的Column
                ColumnPairMode mode = viewColumnPairModes.get(eventData.getTableId());
                eventData.setColumns(columnFilter(eventData.getColumns(), columns, mode));
                eventData.setKeys(columnFilter(eventData.getKeys(), columns, mode));
                if (!CollectionUtils.isEmpty(eventData.getOldKeys())) {
                    eventData.setOldKeys(columnFilter(eventData.getOldKeys(), columns, mode));
                }

                if (CollectionUtils.isEmpty(eventData.getKeys())) { // 无主键，报错
                    throw new ExtractException(
                                               String.format("eventData after viewExtractor has no pks , pls check! identity:%s, new eventData:%s",
                                                             dbBatch.getRowBatch().getIdentity().toString(),
                                                             eventData.toString()));
                }

                // insert：可能view视图只有主键字段，针对无字段情况需要通过
                // delete: eventData本身就没有字段信息，针对无字段情况需要通过
                // update: 过滤后如果无字段(变更需要同步)和主键变更，则可以忽略之，避免sql语法错误
                if (eventData.getEventType().isUpdate()
                    && (CollectionUtils.isEmpty(eventData.getColumns()) || CollectionUtils.isEmpty(eventData.getUpdatedColumns()))
                    && CollectionUtils.isEmpty(eventData.getOldKeys())) {
                    // 过滤之后无字段需要同步，并且不存在主键变更同步，则忽略该记录
                    removeDatas.add(eventData);
                }
            }

        }

        if (!CollectionUtils.isEmpty(removeDatas)) {
            eventDatas.removeAll(removeDatas);
        }
    }

    private List<EventColumn> columnFilter(List<EventColumn> eventColumns, List<ColumnPair> columnPairs,
                                           ColumnPairMode mode) {
        if (mode == null) {
            mode = ColumnPairMode.INCLUDE;
        }

        List<EventColumn> tempColumns = new ArrayList<EventColumn>();
        Map<String, ColumnPair> viewNames = new HashMap<String, ColumnPair>();
        for (ColumnPair columnPair : columnPairs) {
            viewNames.put(StringUtils.lowerCase(columnPair.getSourceColumn().getName()), columnPair);
        }

        for (EventColumn eventColumn : eventColumns) {
            if (mode.isInclude() && viewNames.containsKey(StringUtils.lowerCase(eventColumn.getColumnName()))) {
                tempColumns.add(eventColumn); // 正向匹配
            } else if (mode.isExclude() && !viewNames.containsKey(StringUtils.lowerCase(eventColumn.getColumnName()))) {
                // 逆向匹配
                tempColumns.add(eventColumn);
            }

        }
        return tempColumns;
    }
}
