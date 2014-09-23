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

package com.alibaba.otter.shared.communication.rmi;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.communication.core.CommunicationRegistry;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnection;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnectionFactory;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnectionPoolFactory;
import com.alibaba.otter.shared.communication.core.impl.rmi.RmiCommunicationConnectionFactory;
import com.alibaba.otter.shared.communication.core.impl.rmi.RmiCommunicationEndpoint;
import com.alibaba.otter.shared.communication.core.model.CommunicationParam;
import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.communication.core.model.EventType;
import com.alibaba.otter.shared.communication.core.model.heart.HeartEvent;

/**
 * 测试下rmi的连接池
 * 
 * @author jianghang
 */
public class RmiConnectionTest extends org.jtester.testng.JTester {

    @BeforeClass
    public void initial() {
        // 创建endpoint
        RmiCommunicationEndpoint endpoint = new RmiCommunicationEndpoint(1099);
        endpoint.setAlwaysCreateRegistry(false);
        endpoint.initial();
    }

    @Test
    public void testSingle() {
        CommunicationConnectionFactory factory = new RmiCommunicationConnectionFactory();
        CommunicationParam param = new CommunicationParam();
        param.setIp("127.0.0.1");
        param.setPort(1099);
        CommunicationConnection connection = factory.createConnection(param);
        Object result = connection.call(new HeartEvent());
        want.object(result).notNull();
    }

    @Test
    public void testPool() {
        CommunicationConnectionFactory factory = new RmiCommunicationConnectionFactory();
        CommunicationConnectionFactory poolFactory = new CommunicationConnectionPoolFactory(factory);
        ((CommunicationConnectionPoolFactory) poolFactory).initial();
        CommunicationParam param = new CommunicationParam();
        param.setIp("127.0.0.1");
        param.setPort(1099);
        CommunicationRegistry.regist(PoolEventType.pool, new TestPoolService());

        CommunicationConnection last = null;
        for (int i = 0; i < 11; i++) {
            CommunicationConnection connection = null;
            try {
                connection = poolFactory.createConnection(param);
                connection.call(new PoolEvent(PoolEventType.pool));
                last = connection;
                if (last != null) { // 检查链接是否是重用
                    want.object(last).isEqualTo(connection);
                }
            } finally {
                connection.close();
            }
        }
    }

    @Test
    public void testPool_exhaust() {
        CommunicationConnectionFactory factory = new RmiCommunicationConnectionFactory();
        CommunicationConnectionFactory poolFactory = new CommunicationConnectionPoolFactory(factory);
        ((CommunicationConnectionPoolFactory) poolFactory).initial();
        CommunicationParam param = new CommunicationParam();
        param.setIp("127.0.0.1");
        param.setPort(1099);
        CommunicationRegistry.regist(PoolEventType.exhaust, new TestPoolService());

        ExecutorService executor = Executors.newCachedThreadPool();
        long start = System.currentTimeMillis();
        final CountDownLatch count = new CountDownLatch(11);
        for (int i = 0; i < 11; i++) {
            final CommunicationConnection connection = poolFactory.createConnection(param);
            final PoolEvent event = new PoolEvent(PoolEventType.exhaust);
            event.setSleep(1000);
            executor.submit(new Callable() {

                public Object call() throws Exception {
                    try {
                        Object obj = connection.call(event);
                        count.countDown();
                        return obj;
                    } finally {
                        connection.close();
                    }
                }
            });
        }
        try {
            count.await();
        } catch (InterruptedException e) {
            want.fail();
        }
        long end = System.currentTimeMillis();
        want.number(end - start).isGe(1500L).isLe(2500L);
    }

    public static class PoolEvent extends Event {

        private static final long serialVersionUID = -7387998054696636166L;

        public PoolEvent(PoolEventType event){
            super(event);
        }

        private long sleep;

        public long getSleep() {
            return sleep;
        }

        public void setSleep(long sleep) {
            this.sleep = sleep;
        }

    }

    public static enum PoolEventType implements EventType {
        pool, exhaust;
    }

    public static class TestPoolService {

        public void onPool(PoolEvent event) {
        }

        public void onExhaust(PoolEvent event) {
            try {
                Thread.sleep(event.getSleep());
            } catch (InterruptedException e) {
                want.fail();
            }
        }
    }
}
