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

package com.alibaba.otter.manager.web.home.module.action;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.alibaba.citrus.service.form.CustomErrors;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.datacolumnpair.DataColumnPairGroupService;
import com.alibaba.otter.shared.common.model.config.data.Column;
import com.alibaba.otter.shared.common.model.config.data.ColumnGroup;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;

/**
 * 类ColumnPairGroupAction.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-4-24 下午4:37:18
 */
public class ColumnPairGroupAction {

    private static final String        COLON = ":";

    @Resource(name = "dataColumnPairGroupService")
    private DataColumnPairGroupService dataColumnPairGroupService;

    public void doSave(@Param("dataMediaPairId") Long dataMediaPairId, @Param("submitKey") String submitKey,
                       @Param("pipelineId") Long pipelineId,
                       @FormGroup("columnPairGroupInfo") Group columnPairGroupInfo,
                       @FormField(name = "formColumnPairGroupError", group = "columnPairGroupInfo") CustomErrors err,
                       Navigator nav) throws Exception {
        String[] columnPairStrings = columnPairGroupInfo.getField("groupResult").getStringValues();
        ColumnGroup columnGroup = new ColumnGroup();
        List<ColumnPair> columnPairs = new ArrayList<ColumnPair>();
        for (String columnPairString : columnPairStrings) {
            ColumnPair columnPair = new ColumnPair();
            String[] temp = columnPairString.split(COLON);
            columnPair.setSourceColumn(new Column(temp[0]));
            columnPair.setTargetColumn(new Column(temp[1]));
            columnPair.setDataMediaPairId(dataMediaPairId);
            columnPairs.add(columnPair);
        }

        columnGroup.setColumnPairs(columnPairs);
        columnGroup.setDataMediaPairId(dataMediaPairId);

        dataColumnPairGroupService.removeByDataMediaPairId(dataMediaPairId);
        dataColumnPairGroupService.create(columnGroup);

        nav.redirectToLocation("dataMediaPairList.htm?pipelineId=" + pipelineId);
    }
}
