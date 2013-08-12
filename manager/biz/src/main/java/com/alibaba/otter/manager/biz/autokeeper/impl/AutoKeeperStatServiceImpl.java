package com.alibaba.otter.manager.biz.autokeeper.impl;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.otter.manager.biz.autokeeper.AutoKeeperStatService;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperConnectionStat;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperEphemeralStat;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperServerStat;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperWatchStat;

/**
 * 提供autokeeper相关的数据查询接口
 * 
 * @author simon 2012-9-28 上午11:00:07
 * @version 4.1.0
 */
public class AutoKeeperStatServiceImpl implements AutoKeeperStatService {

    private AutoKeeperData autoKeeperData;

    public AutoKeeperServerStat findServerStat(String serverAddress) {
        return autoKeeperData.getServerStats().get(serverAddress);
    }

    public AutoKeeperServerStat findServerStatBySessionId(String sessionId) {
        String serverAddress = autoKeeperData.getConnectionStats().get(sessionId).getServerAddress();
        return findServerStat(serverAddress);
    }

    public AutoKeeperConnectionStat findConnectionBySessionId(String sessionId) {
        return autoKeeperData.getConnectionStats().get(sessionId);
    }

    public AutoKeeperConnectionStat findConnectionByEphemeralPath(String path) {
        for (AutoKeeperConnectionStat connection : autoKeeperData.getConnectionStats().values()) {
            for (AutoKeeperEphemeralStat ephemeral : connection.getEphemeralStats()) {
                if (ephemeral.getPaths().contains(path)) {
                    return connection;
                }
            }
        }

        return null;
    }

    public List<AutoKeeperConnectionStat> findConnectionByWatcherPath(String path) {
        List<AutoKeeperConnectionStat> connections = new ArrayList<AutoKeeperConnectionStat>();
        for (AutoKeeperConnectionStat connection : autoKeeperData.getConnectionStats().values()) {
            for (AutoKeeperWatchStat watch : connection.getWatchStats()) {
                if (watch.getPaths().contains(path)) {
                    connections.add(connection);
                }
            }
        }

        return connections;
    }

    public void setAutoKeeperData(AutoKeeperData autoKeeperData) {
        this.autoKeeperData = autoKeeperData;
    }

}
