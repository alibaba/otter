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

package com.alibaba.otter.node.etl.select;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jtester.annotations.SpringBeanByName;
import org.jtester.core.TestedObject;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.Header;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.alibaba.otter.canal.protocol.position.LogIdentity;
import com.alibaba.otter.canal.store.model.Event;
import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.BaseOtterTest;
import com.alibaba.otter.node.etl.select.selector.canal.OtterDownStreamHandler;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;

public class OtterDownStreamHandlerIntergration extends BaseOtterTest {

    @SpringBeanByName
    private ConfigClientService configClientService;

    public OtterDownStreamHandlerIntergration(){
        ZooKeeperClient client = new ZooKeeperClient();
        client.setCluster("127.0.0.1:2181");
    }

    @BeforeClass
    public void setup() {
        System.setProperty("nid", "14");

    }

    @Test
    public void testSimple() {
        final OtterDownStreamHandler handler = new OtterDownStreamHandler();
        handler.setPipelineId(388L);
        handler.setDetectingIntervalInSeconds(1);

        ((AutowireCapableBeanFactory) TestedObject.getSpringBeanFactory()).autowireBeanProperties(handler,
            AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
            false);

        final CountDownLatch count = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(new Runnable() {

            public void run() {
                int times = 50;
                handler.before(Arrays.asList(buildEvent()));
                while (--times > 0) {
                    try {
                        Thread.sleep(50000);
                    } catch (InterruptedException e) {
                    }

                    handler.before(Arrays.asList(buildEvent()));
                }

                count.countDown();
            }
        });

        try {
            count.await();
        } catch (InterruptedException e) {
        }
    }

    private Event buildEvent() {
        Event event = new Event();
        event.setLogIdentity(new LogIdentity());

        Header.Builder headBuilder = Header.newBuilder();
        headBuilder.setEventLength(1000L);
        headBuilder.setExecuteTime(new Date().getTime());
        headBuilder.setLogfileName("mysql-bin.000001");
        headBuilder.setLogfileOffset(1000L);
        headBuilder.setSchemaName("test");
        headBuilder.setTableName("ljh");

        Entry.Builder entryBuilder = Entry.newBuilder();
        entryBuilder.setHeader(headBuilder.build());
        entryBuilder.setEntryType(EntryType.ROWDATA);

        RowChange.Builder rowChangeBuilder = RowChange.newBuilder();
        RowData.Builder rowDataBuilder = RowData.newBuilder();
        rowChangeBuilder.addRowDatas(rowDataBuilder.build());

        entryBuilder.setStoreValue(rowChangeBuilder.build().toByteString());
        Entry entry = entryBuilder.build();
        event.setEntry(entry);
        return event;
    }
}
