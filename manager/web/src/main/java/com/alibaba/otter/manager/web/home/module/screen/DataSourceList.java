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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.manager.web.common.model.SeniorDataMediaSource;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.model.config.data.mq.MqMediaSource;

public class DataSourceList {

    @Resource(name = "dataMediaSourceService")
    private DataMediaSourceService dataMediaSourceService;

    @Resource(name = "dataMediaService")
    private DataMediaService       dataMediaService;

    public void execute(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey, Context context)
                                                                                                                 throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> condition = new HashMap<String, Object>();
        if ("请输入关键字(目前支持DataSource的ID、名字搜索)".equals(searchKey)) {
            searchKey = "";
        }
        condition.put("searchKey", searchKey);

        int count = dataMediaSourceService.getCount(condition);
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());

        List<DataMediaSource> dataMediaSources = dataMediaSourceService.listByCondition(condition);
        List<SeniorDataMediaSource> seniorDataMediaSources = new ArrayList<SeniorDataMediaSource>();
        for (DataMediaSource dataMediaSource : dataMediaSources) {

            SeniorDataMediaSource seniorDataMediaSource = new SeniorDataMediaSource();
            seniorDataMediaSource.setEncode(dataMediaSource.getEncode());
            seniorDataMediaSource.setGmtCreate(dataMediaSource.getGmtCreate());
            seniorDataMediaSource.setGmtModified(dataMediaSource.getGmtModified());
            seniorDataMediaSource.setId(dataMediaSource.getId());
            seniorDataMediaSource.setName(dataMediaSource.getName());
            seniorDataMediaSource.setType(dataMediaSource.getType());
            if (dataMediaSource instanceof DbMediaSource) {
                seniorDataMediaSource.setDriver(((DbMediaSource) dataMediaSource).getDriver());
                seniorDataMediaSource.setUrl(((DbMediaSource) dataMediaSource).getUrl());
                seniorDataMediaSource.setUsername(((DbMediaSource) dataMediaSource).getUsername());
            } else if (dataMediaSource instanceof MqMediaSource) {
                seniorDataMediaSource.setUrl(((MqMediaSource) dataMediaSource).getUrl());
                seniorDataMediaSource.setStorePath(((MqMediaSource) dataMediaSource).getStorePath());
            }
            List<DataMedia> dataMedia = dataMediaService.listByDataMediaSourceId(dataMediaSource.getId());
            seniorDataMediaSource.setDataMedias(dataMedia);
            if (dataMedia.size() < 1) {
                seniorDataMediaSource.setUsed(false);
            } else {
                seniorDataMediaSource.setUsed(true);
            }
            seniorDataMediaSources.add(seniorDataMediaSource);

        }

        context.put("sources", seniorDataMediaSources);
        context.put("paginator", paginator);
        context.put("searchKey", searchKey);
    }
}
