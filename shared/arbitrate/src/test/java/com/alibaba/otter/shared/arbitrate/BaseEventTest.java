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

    private String    cluster1  = "127.0.0.1:2188";
    private String    cluster2  = "127.0.0.1:2188,127.0.0.1:2188";
    private ZkClientx zookeeper = null;

    public ZkClientx getZookeeper() {
        // ReflectionUtils.setField(zookeeperField, new ZooKeeperClient(),
        // null);
        Mockit.setUpMock(ZooKeeperClient.class, new Object() {

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
