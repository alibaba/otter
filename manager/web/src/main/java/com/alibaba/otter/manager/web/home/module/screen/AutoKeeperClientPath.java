package com.alibaba.otter.manager.web.home.module.screen;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.autokeeper.AutoKeeperStatService;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperConnectionStat;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperServerStat;

public class AutoKeeperClientPath {

    @Resource(name = "autoKeeperClusterService")
    private AutoKeeperClusterService autoKeeperClusterService;

    @Resource(name = "autoKeeperStatService")
    private AutoKeeperStatService    autoKeeperStatService;

    public void execute(@Param("clusterId") String clusterId, @Param("address") String address, Context context)
                                                                                                                throws Exception {

        AutoKeeperCluster autoKeeperCluster = autoKeeperClusterService.findAutoKeeperClusterById(Long.valueOf(clusterId));
        Set<AutoKeeperConnectionStat> autoKeeperConnectionStats = new HashSet<AutoKeeperConnectionStat>();
        for (String ipAddress : autoKeeperCluster.getServerList()) {
            if (ipAddress.equalsIgnoreCase(address)) {
                AutoKeeperServerStat autoKeeperServerStat = autoKeeperStatService.findServerStat(ipAddress);
                if (autoKeeperServerStat != null) {
                    autoKeeperConnectionStats = autoKeeperServerStat.getConnectionStats();
                } else {
                    autoKeeperConnectionStats = new HashSet<AutoKeeperConnectionStat>();
                }
            }
        }

        context.put("autoKeeperConnectionStats", autoKeeperConnectionStats);
    }
}
