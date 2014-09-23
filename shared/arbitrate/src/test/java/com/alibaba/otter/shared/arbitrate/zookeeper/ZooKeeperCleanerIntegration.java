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
