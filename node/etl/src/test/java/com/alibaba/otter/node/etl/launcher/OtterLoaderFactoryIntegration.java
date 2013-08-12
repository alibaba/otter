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
