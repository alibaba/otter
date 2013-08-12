package com.alibaba.otter.manager.web.home.module.screen;

import java.util.List;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;

public class DataSourceInfo {

    @Resource(name = "dataMediaSourceService")
    private DataMediaSourceService dataMediaSourceService;

    @Resource(name = "dataMediaService")
    private DataMediaService       dataMediaService;

    public void execute(@Param("dataMediaSourceId") Long dataMediaSourceId, Context context) throws Exception {
        DataMediaSource dataMediaSource = dataMediaSourceService.findById(dataMediaSourceId);

        // 查询dataSource关联的同步任务
        List<DataMedia> dataMedias = dataMediaService.listByDataMediaSourceId(dataMediaSource.getId());
        context.put("source", dataMediaSource);
        context.put("dataMedias", dataMedias);
    }
}
