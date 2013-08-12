package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.otter.manager.biz.config.parameter.SystemParameterService;

public class SystemParameter {

    @Resource(name = "systemParameterService")
    private SystemParameterService systemParameterService;

    public void execute(Context context) throws Exception {
        com.alibaba.otter.shared.common.model.config.parameter.SystemParameter systemParameter = systemParameterService.find();
        context.put("systemParameter", systemParameter);
    }
}
