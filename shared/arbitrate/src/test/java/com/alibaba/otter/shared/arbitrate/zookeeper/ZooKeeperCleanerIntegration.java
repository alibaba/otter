package com.alibaba.otter.shared.arbitrate.zookeeper;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 递归删除zookeeper下的otter相关的所有节点
 * 
 * @author jianghang 2011-9-21 下午03:03:31
 * @version 4.0.0
 */
public class ZooKeeperCleanerIntegration extends BaseEventTest {

    private ZkClientx zookeeper = null;

    @BeforeClass
    public void init() {
        zookeeper = getZookeeper();
    }

    @Test
    public void testCleaner() {
        cleaner(ArbitrateConstants.NODE_CHANNEL_ROOT);
        cleaner(ArbitrateConstants.NODE_NID_ROOT);

    }

    private void cleaner(String path) {
        List<String> nodes = zookeeper.getChildren(path);
        for (String node : nodes) {
            cleaner(path + "/" + node);
        }
        if (path.equals(ArbitrateConstants.NODE_CHANNEL_ROOT) || path.equals(ArbitrateConstants.NODE_NID_ROOT)) {
            return;
        } else {
            System.out.println("clean :" + path);
            zookeeper.delete(path);
            return;
        }
    }
}
