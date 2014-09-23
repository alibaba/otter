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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.manager.biz.config.datamatrix.DataMatrixService;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.manager.web.common.model.SeniorDataMatrix;
import com.alibaba.otter.shared.common.model.config.data.DataMatrix;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;

/**
 * @author sarah.lij 2012-7-26 下午04:25:52
 */
public class DataMatrixList {

    @Resource(name = "dataMatrixService")
    private DataMatrixService      dataMatrixService;

    @Resource(name = "dataMediaSourceService")
    private DataMediaSourceService dataMediaSourceService;

    public void execute(@Param("pageIndex") int pageIndex, @Param("matrixId") Long matrixId,
                        @Param("groupKey") String groupKey, @Param("searchKey") String searchKey, Context context)
                                                                                                                  throws Exception {
        Map<String, Object> condition = new HashMap<String, Object>();
        if ("请输入关键字(目前支持Node的ID、名字搜索)".equals(searchKey)) {
            searchKey = "";
        }
        condition.put("searchKey", searchKey);

        int count = dataMatrixService.getCount(condition);
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());
        List<DataMatrix> matrixs = new ArrayList<DataMatrix>();
        if ((null != matrixId) && (matrixId != 0l)) {
            DataMatrix matrix = dataMatrixService.findById(matrixId);
            matrixs.add(matrix);
            paginator.setItems(1);
            paginator.setPage(0);
            searchKey = String.valueOf(matrixId); // 定义为新的searchKey
        } else if (StringUtils.isNotEmpty(groupKey)) {
            DataMatrix matrix = dataMatrixService.findByGroupKey(groupKey);
            matrixs.add(matrix);
            paginator.setItems(1);
            paginator.setPage(0);
            searchKey = String.valueOf(groupKey); // 定义为新的searchKey
        } else {
            matrixs = dataMatrixService.listByCondition(condition);
        }

        List<SeniorDataMatrix> seniorMatrixs = new ArrayList<SeniorDataMatrix>();
        for (DataMatrix matrix : matrixs) {
            SeniorDataMatrix seniorMatrix = new SeniorDataMatrix();
            seniorMatrix.setId(matrix.getId());
            seniorMatrix.setGroupKey(matrix.getGroupKey());
            seniorMatrix.setMaster(matrix.getMaster());
            seniorMatrix.setSlave(matrix.getSlave());
            seniorMatrix.setGmtCreate(matrix.getGmtCreate());
            seniorMatrix.setGmtModified(matrix.getGmtModified());

            Map dataSourceCondition = new HashMap();
            condition.put("searchKey", "jdbc:mysql://groupKey=" + matrix.getGroupKey());
            List<DataMediaSource> dataSources = dataMediaSourceService.listByCondition(dataSourceCondition);
            seniorMatrix.setUsed(!CollectionUtils.isEmpty(dataSources));

            seniorMatrixs.add(seniorMatrix);
        }

        context.put("dataMatrixs", seniorMatrixs);
        context.put("paginator", paginator);
        context.put("searchKey", searchKey);
    }
}
