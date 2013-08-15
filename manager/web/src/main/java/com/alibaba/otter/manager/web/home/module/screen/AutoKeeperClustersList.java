package com.alibaba.otter.manager.web.home.module.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;

public class AutoKeeperClustersList {

    @Resource(name = "autoKeeperClusterService")
    private AutoKeeperClusterService autoKeeperClusterService;

    public void execute(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey, Context context)
                                                                                                                 throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> condition = new HashMap<String, Object>();
        if ("请输入关键字(目前支持Zookeeper的地址搜索)".equals(searchKey)) {
            searchKey = "";
        }
        condition.put("searchKey", searchKey);

        int count = autoKeeperClusterService.getCount();
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());

        List<AutoKeeperCluster> autoKeeperClusters = autoKeeperClusterService.listAutoKeeperClusters();

        context.put("paginator", paginator);
        context.put("autoKeeperClusters", autoKeeperClusters);
    }
}
