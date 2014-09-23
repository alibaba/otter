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

import java.util.List;
import java.util.Map;

import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperConnectionStat;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperEphemeralStat;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperServerStat;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperWatchStat;
import com.google.common.collect.MapMaker;

/**
 * join server之间的统计数据，提供当前最新和完整的数据结构方便数据查询
 * 
 * <pre>
 * server -> 
 *        connection -> 
 *               ->  watcher
 *               ->  ephemeral
 * </pre>
 * 
 * @author jianghang 2012-9-21 下午03:02:00
 * @version 4.1.0
 */
public class AutoKeeperData implements AutoKeeperPersist {

    private Map<String, AutoKeeperServerStat>     serverStats     = new MapMaker().makeMap(); // serverIp和server的对应关系
    private Map<String, AutoKeeperConnectionStat> connectionStats = new MapMaker().makeMap(); // sessionId和connection的对应关系

    public void joinServer(String address, AutoKeeperServerStat summary) {
        serverStats.put(address, summary);
    }

    public void joinConnection(String address, List<AutoKeeperConnectionStat> connections) {
        // 记录connection引用
        for (AutoKeeperConnectionStat connection : connections) {
            connectionStats.put(connection.getSessionId(), connection);
        }

        // 添加引用到server中
        if (serverStats.containsKey(address)) {
            serverStats.get(address).getConnectionStats().addAll(connections);
        }
    }

    public void joinEphemeral(String address, List<AutoKeeperEphemeralStat> ephemerals) {
        for (AutoKeeperEphemeralStat ephemeral : ephemerals) {
            if (connectionStats.containsKey(ephemeral.getSessionId())) {
                // 找到对应的connection进行关联，填充数据
                connectionStats.get(ephemeral.getSessionId()).getEphemeralStats().add(ephemeral);
            }
        }
    }

    public void joinWatch(String address, List<AutoKeeperWatchStat> watches) {
        for (AutoKeeperWatchStat watch : watches) {
            if (connectionStats.containsKey(watch.getSessionId())) {
                // 找到对应的connection进行关联，填充数据
                connectionStats.get(watch.getSessionId()).getWatchStats().add(watch);
            }
        }
    }

    public void persist() {
        serverStats.clear();
        connectionStats.clear();
    }

    public Map<String, AutoKeeperServerStat> getServerStats() {
        return serverStats;
    }

    public Map<String, AutoKeeperConnectionStat> getConnectionStats() {
        return connectionStats;
    }

}
