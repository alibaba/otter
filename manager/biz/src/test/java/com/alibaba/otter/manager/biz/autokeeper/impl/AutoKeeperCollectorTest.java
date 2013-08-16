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

    private final static String   ADDRESS = "10.20.144.51:2181";

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
