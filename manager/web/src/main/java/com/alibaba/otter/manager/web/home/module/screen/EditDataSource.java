package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;

/**
 * 类EditDataSource.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-10-26 下午04:03:14
 */
public class EditDataSource {

    @Resource(name = "dataMediaSourceService")
    private DataMediaSourceService dataMediaSourceService;

    public void execute(@Param("dataMediaSourceId") Long dataMediaSourceId, @Param("pageIndex") int pageIndex,
                        @Param("searchKey") String searchKey, Context context) throws Exception {
        DataMediaSource source = dataMediaSourceService.findById(dataMediaSourceId);
        context.put("source", source);
        context.put("pageIndex", pageIndex);
        context.put("searchKey", searchKey);
    }

}
