package com.alibaba.otter.manager.web.home.module.screen;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;

/**
 * @author jianghang 2011-8-31 下午07:00:19
 */
public class Ok {

    public void execute(@Param(name = "param") String param, Context context) {
        context.put("ok", param);
    }
}
