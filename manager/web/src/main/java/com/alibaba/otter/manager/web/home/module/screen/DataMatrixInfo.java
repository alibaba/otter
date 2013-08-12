package com.alibaba.otter.manager.web.home.module.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.datamatrix.DataMatrixService;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.shared.common.model.config.data.DataMatrix;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;

public class DataMatrixInfo {

    @Resource(name = "dataMatrixService")
    private DataMatrixService      dataMatrixService;

    @Resource(name = "dataMediaSourceService")
    private DataMediaSourceService dataMediaSourceService;

    public void execute(@Param("matrixId") Long matrixId, Context context) throws Exception {
        DataMatrix matrix = dataMatrixService.findById(matrixId);

        Map condition = new HashMap();
        condition.put("searchKey", "jdbc:mysql://groupKey=" + matrix.getGroupKey());
        List<DataMediaSource> dataSources = dataMediaSourceService.listByCondition(condition);

        context.put("dataMatrix", matrix);
        context.put("dataSources", dataSources);
    }
}
