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

import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.otter.manager.biz.config.parameter.SystemParameterService;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;

public class SystemParameterAction extends AbstractAction {

    @Resource(name = "systemParameterService")
    private SystemParameterService systemParameterService;

    /**
     * 修改系统参数
     */
    public void doEdit(@FormGroup("systemParameterDetailInfo") Group systemParameterDetailInfo, Navigator nav)
                                                                                                              throws Exception {

        SystemParameter systemParameter = new SystemParameter();
        systemParameterDetailInfo.setProperties(systemParameter);
        systemParameterService.createOrUpdate(systemParameter);
        nav.redirectToLocation("systemParameter.htm?");
    }
}
