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

package com.alibaba.otter.node.etl.common.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.jtester.annotations.SpringBeanByName;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.BaseDbTest;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
import com.alibaba.otter.node.etl.common.db.dialect.mysql.MysqlDialect;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.model.config.data.db.DbDataMedia;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;

public class DbPerfIntergration extends BaseDbTest {

    @SpringBeanByName
    private DbDialectFactory dbDialectFactory;

    @Test
    public void test_stack() {
        DbMediaSource dbMediaSource = new DbMediaSource();
        dbMediaSource.setId(1L);
        dbMediaSource.setDriver("com.mysql.jdbc.Driver");
        dbMediaSource.setUsername("otter");
        dbMediaSource.setPassword("otter");
        dbMediaSource.setUrl("jdbc:mysql://127.0.0.1:3306/retl");
        dbMediaSource.setEncode("UTF-8");
        dbMediaSource.setType(DataMediaType.MYSQL);

        DbDataMedia dataMedia = new DbDataMedia();
        dataMedia.setSource(dbMediaSource);
        dataMedia.setId(1L);
        dataMedia.setName("ljhtable1");
        dataMedia.setNamespace("otter");

        final DbDialect dbDialect = dbDialectFactory.getDbDialect(2L, dataMedia.getSource());
        want.object(dbDialect).clazIs(MysqlDialect.class);

        final TransactionTemplate transactionTemplate = dbDialect.getTransactionTemplate();

        // 插入数据准备
        int minute = 5;
        int nextId = 1;
        final int thread = 10;
        final int batch = 50;
        final String sql = "insert into otter.ljhtable1 values(? , ? , ? , ?)";

        final CountDownLatch latch = new CountDownLatch(thread);
        ExecutorService executor = new ThreadPoolExecutor(thread,
            thread,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue(thread * 2),
            new NamedThreadFactory("load"),
            new ThreadPoolExecutor.CallerRunsPolicy());

        for (int sec = 0; sec < minute * 60; sec++) {
            // 执行秒循环
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < thread; i++) {
                final int start = nextId + i * batch;
                executor.submit(new Runnable() {

                    public void run() {
                        try {
                            transactionTemplate.execute(new TransactionCallback() {

                                public Object doInTransaction(TransactionStatus status) {
                                    JdbcTemplate jdbcTemplate = dbDialect.getJdbcTemplate();
                                    return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

                                        public void setValues(PreparedStatement ps, int idx) throws SQLException {
                                            int id = start + idx;
                                            StatementCreatorUtils.setParameterValue(ps, 1, Types.INTEGER, null, id);
                                            StatementCreatorUtils.setParameterValue(ps,
                                                2,
                                                Types.VARCHAR,
                                                null,
                                                RandomStringUtils.randomAlphabetic(1000));
                                            // RandomStringUtils.randomAlphabetic()
                                            long time = new Date().getTime();
                                            StatementCreatorUtils.setParameterValue(ps,
                                                3,
                                                Types.TIMESTAMP,
                                                new Timestamp(time));
                                            StatementCreatorUtils.setParameterValue(ps,
                                                4,
                                                Types.TIMESTAMP,
                                                new Timestamp(time));
                                        }

                                        public int getBatchSize() {
                                            return batch;
                                        }
                                    });
                                }
                            });
                        } finally {
                            latch.countDown();
                        }
                    }
                });

            }

            long endTime = System.currentTimeMillis();
            try {
                latch.await(1000 * 60L - (endTime - startTime), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (latch.getCount() != 0) {
                System.out.println("perf is not enough!");
                System.exit(-1);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Time cost : " + (System.currentTimeMillis() - startTime));
            try {
                TimeUnit.MILLISECONDS.sleep(1000L - (endTime - startTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            nextId = nextId + thread * batch;
        }
        executor.shutdown();
    }
}
