package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.manager.biz.config.canal.CanalService;

public class CanalInfo {

    @Resource(name = "canalService")
    private CanalService canalService;

    public void execute(@Param("canalId") Long canalId, Context context) throws Exception {
        Canal canal = canalService.findById(canalId);
        context.put("canal", canal);
    }
}
