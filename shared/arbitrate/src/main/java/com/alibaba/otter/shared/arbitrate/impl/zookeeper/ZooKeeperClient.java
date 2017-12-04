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

package com.alibaba.otter.shared.arbitrate.impl.zookeeper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.I0Itec.zkclient.IZkStateListener;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;
import com.google.common.base.Function;
import com.google.common.collect.OtterMigrateMap;

/**
 * 封装了ZooKeeper，使其支持节点的优先顺序，比如美国机房的节点会优先加载美国对应的zk集群列表，都失败后才会选择加载杭州的zk集群列表
 * 
 * @author jianghang 2011-9-8 下午07:55:44
 * @version 4.0.0
 */
public class ZooKeeperClient {

    private static String               cluster;
    private static int                  sessionTimeout = 10 * 1000;
    private static Map<Long, ZkClientx> clients        = OtterMigrateMap.makeComputingMap(new Function<Long, ZkClientx>() {

                                                           public ZkClientx apply(Long pipelineId) {
                                                               return createClient();
                                                           }
                                                       });
    private static Long                 defaultId      = 0L;

    /**
     * 获取对应的zookeeper客户端
     */
    public static ZkClientx getInstance() {
        return getInstance(defaultId);
    }

    /**
     * 根据pipelineId获取对应的zookeeper客户端，每个pipelineId可以独立一个zookeeper链接，保证性能
     */
    public static ZkClientx getInstance(Long pipelineId) {
        return clients.get(pipelineId);
    }

    public static void destory() {
        for (ZkClientx zkClient : clients.values()) {
            zkClient.close();
        }
    }

    public static void registerNotification(final SessionExpiredNotification notification) {
        getInstance().subscribeStateChanges(new IZkStateListener() {

            public void handleStateChanged(KeeperState state) throws Exception {

            }

            public void handleNewSession() throws Exception {
                notification.notification();
            }

            @Override
            public void handleSessionEstablishmentError(Throwable error) throws Exception {
            }
        });

    }

    private static ZkClientx createClient() {
        List<String> serveraddrs = getServerAddrs();
        return new ZkClientx(StringUtils.join(serveraddrs, ","), sessionTimeout);
    }

    /**
     * 从当前的node信息中获取对应的zk集群信息
     */
    private static List<String> getServerAddrs() {
        List<String> result = ArbitrateConfigUtils.getServerAddrs();
        if (result == null || result.size() == 0) {
            result = Arrays.asList(cluster);
        }
        return result;
    }

    public void setCluster(String cluster) {
        ZooKeeperClient.cluster = cluster;
    }

    public void setSessionTimeout(int timeout) {
        ZooKeeperClient.sessionTimeout = timeout;
    }

}
