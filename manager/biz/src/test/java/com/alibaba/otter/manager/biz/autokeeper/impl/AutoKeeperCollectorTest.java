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

package com.alibaba.otter.manager.biz.autokeeper.impl;

import java.util.Set;

import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.Test;

import com.alibaba.otter.manager.biz.BaseOtterTest;
import com.alibaba.otter.manager.biz.autokeeper.AutoKeeperStatService;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperConnectionStat;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperServerStat;

/**
 * @author simon 2012-9-29 下午5:23:59
 * @version 4.1.0
 */
public class AutoKeeperCollectorTest extends BaseOtterTest {

    @SpringBeanByName
    private AutoKeeperCollector   autoKeeperCollector;

    @SpringBeanByName
    private AutoKeeperStatService autoKeeperStatService;

    private final static String   ADDRESS = "127.0.0.1:2181";

    @Test
    public void testCollectorServerStat() {
        autoKeeperCollector.collectorServerStat(ADDRESS);
        autoKeeperCollector.collectorConnectionStat(ADDRESS);
        autoKeeperCollector.collectorWatchStat(ADDRESS);
        autoKeeperCollector.collectorEphemeralStat(ADDRESS);
        AutoKeeperServerStat stat = autoKeeperStatService.findServerStat(ADDRESS);
        Set<AutoKeeperConnectionStat> conns = stat.getConnectionStats();
        for (AutoKeeperConnectionStat autoKeeperConnectionStat : conns) {
            autoKeeperStatService.findConnectionBySessionId(autoKeeperConnectionStat.getSessionId());
            autoKeeperStatService.findServerStatBySessionId(autoKeeperConnectionStat.getSessionId());
            String path = autoKeeperConnectionStat.getClientAddress();
            System.out.println(path);
        }
    }

}
