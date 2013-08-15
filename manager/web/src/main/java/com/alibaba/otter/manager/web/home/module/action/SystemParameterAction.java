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
