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

package com.alibaba.otter.manager.web.home.module.screen.monitor;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.monitor.Monitor;

public class MonitorTrigger {

    private static final Logger log = LoggerFactory.getLogger("monitorTrigger");

    @Resource(name = "globalMonitor")
    private Monitor             globalMonitor;

    public void execute(@Param(name = "token") String token, Context context) throws Exception {

        if (StringUtils.isEmpty(token)) {
            context.put("result", "empty token");
            return;
        }

        if (!verify(token)) {
            context.put("result", "invalided token");
            return;
        }

        try {
            globalMonitor.explore();
        } catch (Throwable e) {
            log.error("monitor trigger happens error", e);
            context.put("result", e);
            return;
        }
        context.put("result", true);
    }

    private boolean verify(String token) {
        // FIXME 简单实现
        return "otter".equals(token);
    }
}
