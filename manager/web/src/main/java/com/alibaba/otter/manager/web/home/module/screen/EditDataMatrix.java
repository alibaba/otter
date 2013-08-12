package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.config.datamatrix.DataMatrixService;
import com.alibaba.otter.shared.common.model.config.data.DataMatrix;

public class EditDataMatrix {

    @Resource(name = "dataMatrixService")
    private DataMatrixService dataMatrixService;

    /**
     * @param context
     * @throws WebxException
     */
    public void execute(@Param("matrixId") Long matrixId, Context context) throws Exception {
        DataMatrix matrix = dataMatrixService.findById(matrixId);
        context.put("dataMatrix", matrix);
    }
}
