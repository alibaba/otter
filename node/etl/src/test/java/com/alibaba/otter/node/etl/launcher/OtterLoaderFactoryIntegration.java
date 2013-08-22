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

package com.alibaba.otter.node.etl.launcher;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.BaseDbTest;
import com.alibaba.otter.node.etl.load.loader.OtterLoaderFactory;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

public class OtterLoaderFactoryIntegration extends BaseDbTest {

    @SpringBeanByName
    private ExecutorService    executorService;

    @SpringBeanByName
    private OtterLoaderFactory otterLoaderFactory;

    @BeforeClass
    public void initial() {
        System.setProperty("nid", "1");
    }

    @Test
    public void test_simple() {
        Identity identity = new Identity();
        identity.setChannelId(100L);
        identity.setPipelineId(100L);
        identity.setProcessId(100L);

        RowBatch rowBatch = new RowBatch();
        rowBatch.setIdentity(identity);

        FileBatch fileBatch = new FileBatch();
        fileBatch.setIdentity(identity);

        final DbBatch dbBatch = new DbBatch();
        dbBatch.setRowBatch(rowBatch);
        dbBatch.setFileBatch(fileBatch);
        final CountDownLatch latch = new CountDownLatch(1);
        executorService.submit(new Runnable() {

            public void run() {
                System.out.println("first run!!!!!!");
                otterLoaderFactory.load(dbBatch);
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
        }
    }
}
