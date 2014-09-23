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
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.manager.biz.config.canal.CanalService;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;

public class EditCanal {

    @Resource(name = "canalService")
    private CanalService             canalService;

    @Resource(name = "autoKeeperClusterService")
    private AutoKeeperClusterService autoKeeperClusterService;

    /**
     * @param context
     * @throws WebxException
     */
    public void execute(@Param("canalId") Long canalId, Context context) throws Exception {
        Canal canal = canalService.findById(canalId);
        List<AutoKeeperCluster> zkClusters = autoKeeperClusterService.listAutoKeeperClusters();
        context.put("zkClusters", zkClusters);
        context.put("canal", canal);
    }
}
