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
