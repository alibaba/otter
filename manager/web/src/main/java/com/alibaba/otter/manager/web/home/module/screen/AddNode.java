package com.alibaba.otter.manager.web.home.module.screen;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.util.CollectionUtils;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;

public class AddNode {

    @Resource(name = "autoKeeperClusterService")
    private AutoKeeperClusterService autoKeeperClusterService;

    public void execute(Context context, Navigator nav) throws Exception {
        List<AutoKeeperCluster> zkClusters = autoKeeperClusterService.listAutoKeeperClusters();

        if (CollectionUtils.isEmpty(zkClusters)) {
            nav.redirectToLocation("addZookeeper.htm?message=init");
        } else {
            context.put("zkClusters", zkClusters);
        }
    }

}
