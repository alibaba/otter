package com.alibaba.otter.manager.web.home.module.screen;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.autokeeper.AutoKeeperStatService;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperServerStat;

public class AutoKeeperClustersDetail {

    @Resource(name = "autoKeeperClusterService")
    private AutoKeeperClusterService autoKeeperClusterService;

    @Resource(name = "autoKeeperStatService")
    private AutoKeeperStatService    autoKeeperStatService;

    public void execute(@Param("clusterId") String clusterId, Context context) throws Exception {

        Map<String, AutoKeeperServerStat> statMap = new HashMap<String, AutoKeeperServerStat>();
        AutoKeeperCluster autoKeeperCluster = autoKeeperClusterService.findAutoKeeperClusterById(Long.valueOf(clusterId));
        for (String address : autoKeeperCluster.getServerList()) {
            AutoKeeperServerStat autoKeeperServerStat = autoKeeperStatService.findServerStat(address);
            statMap.put(address, autoKeeperServerStat);
        }
        context.put("clusterId", clusterId);
        context.put("statMap", statMap);
    }
}
