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

package com.alibaba.otter.manager.biz.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.CollectionUtils;

import com.alibaba.otter.manager.biz.monitor.impl.AbstractRuleMonitor;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;

/**
 * @author zebin.xuzb @ 2012-8-29
 * @version 4.1.0
 */
public class MonitorRuleExplorerRegisty {

    // Map<MonitorName, Map<explorerName, MonitorExplorer>>
    private static Map<MonitorName, Map<String, Monitor>> registy = new ConcurrentHashMap<MonitorName, Map<String, Monitor>>(
                                                                                                                             16);

    public static void register(MonitorName monitorName, Monitor explorer) {
        MonitorRuleExplorerRegisty.register(monitorName, null, explorer);
    }

    synchronized public static void register(MonitorName monitorName, String explorerName, Monitor explorer) {
        if (monitorName == null || explorer == null) {
            return;
        }

        if (!AbstractRuleMonitor.class.isAssignableFrom(explorer.getClass())) {
            throw new UnsupportedOperationException(
                                                    "only accept AbstractRuleMonitorExplorer or it's subclass to regist");
        }

        Map<String, Monitor> explorers = registy.get(monitorName);
        if (explorers == null) {
            explorers = new ConcurrentHashMap<String, Monitor>(16);
            registy.put(monitorName, explorers);
        }

        if (explorerName == null) {
            explorerName = explorer.getClass().getName();
        }

        explorers.put(explorerName, explorer);
    }

    public static Collection<Monitor> findExplorer(MonitorName monitorName) {
        if (monitorName == null) {
            return Collections.EMPTY_LIST;
        }
        Map<String, Monitor> explorers = registy.get(monitorName);
        if (CollectionUtils.isEmpty(explorers)) {
            return Collections.EMPTY_LIST;
        }
        return explorers.values();
    }

    public static void unRegister(MonitorName monitorName, Monitor explorer) {
        if (monitorName == null || explorer == null) {
            return;
        }

        Map<String, Monitor> explorers = registy.get(monitorName);
        if (CollectionUtils.isEmpty(explorers)) {
            return;
        }

        String explorerName = explorer.getClass().getName();
        explorers.remove(explorerName);
    }

    public static void unRegister(MonitorName monitorName, String explorerName) {
        if (monitorName == null || explorerName == null) {
            return;
        }

        Map<String, Monitor> explorers = registy.get(monitorName);
        if (CollectionUtils.isEmpty(explorers)) {
            return;
        }
        explorers.remove(explorerName);
    }

}
