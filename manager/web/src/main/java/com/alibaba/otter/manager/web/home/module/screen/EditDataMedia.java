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
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;

/**
 * 类AddDataMedia.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-10-25 上午10:00:32
 */
public class EditDataMedia {

    @Resource(name = "dataMediaService")
    private DataMediaService dataMediaService;

    public void execute(@Param("dataMediaId") Long dataMediaId, @Param("pageIndex") int pageIndex,
                        @Param("searchKey") String searchKey, Context context) throws Exception {
        DataMedia dataMedia = dataMediaService.findById(dataMediaId);
        context.put("dataMedia", dataMedia);
        context.put("pageIndex", pageIndex);
        context.put("searchKey", searchKey);
    }

}
