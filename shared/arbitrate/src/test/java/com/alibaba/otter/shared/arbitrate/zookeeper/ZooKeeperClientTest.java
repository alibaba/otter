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

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import mockit.Mock;
import mockit.Mockit;

import org.apache.zookeeper.ClientCnxn;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.client.HostProvider;
import org.apache.zookeeper.client.StaticHostProvider;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.ReflectionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.BaseOtterTest;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeSessionExpired;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;
import com.alibaba.otter.shared.common.utils.zookeeper.ZooKeeperx;

/**
 * 测试下zookeeper优先集群列表
 * 
 * @author jianghang 2011-9-16 下午03:00:01
 * @version 4.0.0
 */
public class ZooKeeperClientTest extends BaseOtterTest {

    private static final Field clientCnxnField      = ReflectionUtils.findField(ZooKeeper.class, "cnxn");
    private static final Field hostProviderField    = ReflectionUtils.findField(ClientCnxn.class, "hostProvider");
    private static final Field serverAddressesField = ReflectionUtils.findField(StaticHostProvider.class,
                                                        "serverAddresses");
    static {
        ReflectionUtils.makeAccessible(clientCnxnField);
        ReflectionUtils.makeAccessible(hostProviderField);
        ReflectionUtils.makeAccessible(serverAddressesField);
    }

    private String             cluster1             = "127.0.0.1:2188";
    private String             cluster2             = "127.0.0.1:2188,127.0.0.1:2188";

    // private String cluster1 = "127.0.0.1:2181";
    // private String cluster2 = "127.0.0.1:2181,127.0.0.1:2181";

    @BeforeClass
    public void initial() {
        String data = String.format(Locale.ENGLISH, "%010d", "268171150");
        System.out.println(data);

        Mockit.setUpMock(ZooKeeperClient.class, new Object() {

            @Mock
            private List<String> getServerAddrs() {
                return Arrays.asList(cluster1, cluster2);
            }

        });

        // 初始化节点
        Mockit.setUpMock(NodeSessionExpired.class, new Object() {

            @Mock
            public void notification() {
                return;
            }

        });
    }

    @Test
    public void testClient() {
        ZkClientx zk = ZooKeeperClient.getInstance();
        // 强制获取zk中的地址信息
        final ZooKeeper zkp = ((ZooKeeperx) zk.getConnection()).getZookeeper();
        ClientCnxn cnxn = (ClientCnxn) ReflectionUtils.getField(clientCnxnField, zkp);
        HostProvider hostProvider = (HostProvider) ReflectionUtils.getField(hostProviderField, cnxn);
        List<InetSocketAddress> serverAddrs = (List<InetSocketAddress>) ReflectionUtils.getField(serverAddressesField,
            hostProvider);
        want.number(serverAddrs.size()).isEqualTo(3);
        String s1 = serverAddrs.get(0).getAddress().getHostAddress() + ":" + serverAddrs.get(0).getPort();
        want.string(s1).isEqualTo(cluster1);

        Stat stat = new Stat();
        try {
            zkp.getChildren("/otter/channel/304/388", false, stat);
            System.out.println(stat.getCversion());
        } catch (KeeperException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (InterruptedException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        // 测试下session timeout
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread() {

            public void run() {
                try {
                    zkp.getChildren("/", false);
                } catch (KeeperException e1) {
                    want.fail();
                } catch (InterruptedException e1) {
                    want.fail();
                }
                int sessionTimeout = zkp.getSessionTimeout();
                long sessionId = zkp.getSessionId();
                byte[] passwd = zkp.getSessionPasswd();
                try {
                    ZooKeeper newZk = new ZooKeeper(cluster1, sessionTimeout, new Watcher() {

                        public void process(WatchedEvent event) {
                            // do nothing
                        }

                    }, sessionId, passwd);

                    // 用老的sessionId连接上去，进行一次close操作后，让原先正在使用的出现SESSION_EXPIRED
                    newZk.close();
                } catch (IOException e) {
                    want.fail();
                } catch (InterruptedException e) {
                    want.fail();
                }

                latch.countDown();
            }

        }.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            want.fail();
        }

        zk.getChildren("/");
    }
}
