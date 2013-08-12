package com.alibaba.otter.shared.arbitrate;

import java.util.Arrays;
import java.util.List;

import mockit.Mock;
import mockit.Mockit;

import org.testng.annotations.BeforeClass;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

public class BaseEventTest extends BaseOtterTest {

    // private String cluster1 = "10.20.153.52:2181";
    // private String cluster2 = "10.20.153.51:2182,10.20.153.51:2183";
    private String    cluster1  = "10.20.153.52:2188";
    private String    cluster2  = "10.20.153.52:2188,10.20.153.52:2188";
    private ZkClientx zookeeper = null;

    public ZkClientx getZookeeper() {
        // ReflectionUtils.setField(zookeeperField, new ZooKeeperClient(), null);
        Mockit.setUpMock(ZooKeeperClient.class, new Object() {

            @SuppressWarnings("unused")
            @Mock
            private List<String> getServerAddrs() {
                return Arrays.asList(cluster1, cluster2);
            }

        });

        return ZooKeeperClient.getInstance();
    }

    @BeforeClass
    final public void clean() {
        zookeeper = getZookeeper();
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

    protected void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            want.fail();
        }
    }

    protected void sleep(Long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            want.fail();
        }
    }
}
