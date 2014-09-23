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
