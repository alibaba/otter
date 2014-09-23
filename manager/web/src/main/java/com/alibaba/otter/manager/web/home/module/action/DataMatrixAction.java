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

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.service.form.CustomErrors;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.datamatrix.DataMatrixService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.config.data.DataMatrix;

public class DataMatrixAction extends AbstractAction {

    @Resource(name = "dataMatrixService")
    private DataMatrixService dataMatrixService;

    public void doAdd(@FormGroup("dataMatrixInfo") Group dataMatrixInfo,
                      @FormField(name = "formDataMatrixError", group = "dataMatrixInfo") CustomErrors err, Navigator nav)
                                                                                                                         throws Exception {
        DataMatrix matrix = new DataMatrix();
        dataMatrixInfo.setProperties(matrix);

        try {
            dataMatrixService.create(matrix);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMatrix");
            return;
        }

        nav.redirectTo(WebConstant.MATRIX_LIST_LINK);
    }

    public void doEdit(@FormGroup("dataMatrixInfo") Group dataMatrixInfo,
                       @FormField(name = "formDataMatrixError", group = "dataMatrixInfo") CustomErrors err,
                       Navigator nav) throws Exception {
        DataMatrix matrix = new DataMatrix();
        dataMatrixInfo.setProperties(matrix);

        try {
            dataMatrixService.modify(matrix);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMatrix");
            return;
        }

        nav.redirectToLocation("dataMatrixList.htm?matrixId=" + matrix.getId());
    }

    public void doDelete(@Param("matrixId") Long matrixId, Navigator nav) throws WebxException {
        dataMatrixService.remove(matrixId);
        nav.redirectToLocation("dataMatrixList.htm");
    }

    public void doSwitch(@Param("matrixId") Long matrixId, Navigator nav) throws WebxException {
        DataMatrix matrix = dataMatrixService.findById(matrixId);
        String slave = matrix.getMaster();
        String master = matrix.getSlave();
        if (StringUtils.isNotEmpty(master) && StringUtils.isNotEmpty(slave)) {
            matrix.setMaster(master);
            matrix.setSlave(slave);
        }

        dataMatrixService.modify(matrix);
        nav.redirectToLocation("dataMatrixList.htm?matrixId=" + matrixId);
    }
}
