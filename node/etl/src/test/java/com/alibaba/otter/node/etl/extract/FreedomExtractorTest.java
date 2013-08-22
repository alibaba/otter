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

package com.alibaba.otter.node.etl.extract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import mockit.Mocked;

import org.apache.commons.lang.math.RandomUtils;
import org.jtester.annotations.SpringBeanByName;
import org.jtester.annotations.SpringBeanFrom;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.BaseDbTest;
import com.alibaba.otter.node.etl.extract.extractor.FreedomExtractor;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.db.DbDataMedia;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

class FreedomExtractorTest extends BaseDbTest {

    @SpringBeanByName
    private FreedomExtractor    freedomExtractor;

    @SpringBeanFrom
    @Mocked
    private ConfigClientService configClientService;

    private Identity            identity = null;

    @BeforeMethod
    public void setUp() {
        identity = new Identity();
        identity.setChannelId(100L);
        identity.setPipelineId(100L);
        identity.setProcessId(100L);
    }

    @Test
    public void test_mysql() {
        final Pipeline pipeline = new Pipeline();
        pipeline.setId(100L);

        int start = RandomUtils.nextInt();
        int count = 10;
        List<DataMediaPair> pairs = getDataMediaPairForMysql(start, count);
        pipeline.setPairs(pairs);

        new NonStrictExpectations() {

            {
                configClientService.findPipeline(100L);
                returns(pipeline);
            }
        };

        // 构造数据
        RowBatch rowBatch = new RowBatch();
        rowBatch.setIdentity(identity);
        for (int tableId = start; tableId < start + count; tableId++) {
            for (int i = start; i < start + count; i++) {
                EventData eventData = getEventData(tableId, i);
                eventData.setSchemaName("retl");
                eventData.setTableName("retl_buffer");
                rowBatch.merge(eventData);
            }
        }

        DbBatch dbBatch = new DbBatch(rowBatch);
        freedomExtractor.extract(dbBatch);
        want.collection(dbBatch.getRowBatch().getDatas()).sizeEq(count * count);
    }

    @Test
    public void test_oracle() {
        final Pipeline pipeline = new Pipeline();
        pipeline.setId(100L);

        int start = RandomUtils.nextInt();
        int count = 10;
        List<DataMediaPair> pairs = getDataMediaPairForOracle(start, count);
        pipeline.setPairs(pairs);

        new NonStrictExpectations() {

            {
                configClientService.findPipeline(100L);
                returns(pipeline);
            }
        };

        // 构造数据
        RowBatch rowBatch = new RowBatch();
        rowBatch.setIdentity(identity);
        for (int tableId = start; tableId < start + count; tableId++) {
            for (int i = start; i < start + count; i++) {
                EventData eventData = getEventData(tableId, i);
                eventData.setSchemaName("retl");
                eventData.setTableName("retl_buffer");
                rowBatch.merge(eventData);
            }
        }

        DbBatch dbBatch = new DbBatch(rowBatch);
        freedomExtractor.extract(dbBatch);
        want.collection(dbBatch.getRowBatch().getDatas()).sizeEq(count * count);
    }

    private EventData getEventData(long tableId, int value) {
        EventData eventData = new EventData();
        eventData.setTableId(tableId);
        eventData.setEventType(EventType.INSERT);
        eventData.setExecuteTime(new Date().getTime());
        eventData.setKeys(getPrimary(value));
        eventData.setColumns(getColumn(value));

        return eventData;
    }

    private List<EventColumn> getPrimary(int value) {
        EventColumn pk = new EventColumn();
        pk.setColumnName("id");
        pk.setColumnType(java.sql.Types.INTEGER);
        pk.setColumnValue(String.valueOf(value));
        pk.setIndex(1);
        pk.setNull(false);
        return Arrays.asList(pk);
    }

    private List<EventColumn> getColumn(int value) {
        EventColumn c1 = new EventColumn();
        c1.setColumnName("type");
        c1.setColumnValue("I");

        EventColumn c2 = new EventColumn();
        c2.setColumnName("table_id");
        c2.setColumnValue(String.valueOf(value));

        EventColumn c3 = new EventColumn();
        c3.setColumnName("pk_data");
        c3.setColumnValue(String.valueOf(value) + ((char) 1) + "hello");
        return Arrays.asList(c1, c2, c3);
    }

    private List<DataMediaPair> getDataMediaPairForMysql(long tableId, int count) {
        List<DataMediaPair> pairs = new ArrayList<DataMediaPair>();
        for (int i = 0; i < count; i++) {
            DataMediaPair pair = new DataMediaPair();
            pair.setId(Long.valueOf(i));
            pair.setPullWeight(1L);
            pair.setPushWeight(1L);

            DbDataMedia mysqlMedia = getMysqlMedia();
            mysqlMedia.setId(tableId + i);
            pair.setSource(mysqlMedia);

            DbDataMedia oracleMedia = getOracleMedia();
            oracleMedia.setId(tableId + i + count);
            pair.setTarget(oracleMedia);
            pairs.add(pair);
        }
        return pairs;
    }

    private List<DataMediaPair> getDataMediaPairForOracle(long tableId, int count) {
        List<DataMediaPair> pairs = new ArrayList<DataMediaPair>();
        for (int i = 0; i < count; i++) {
            DataMediaPair pair = new DataMediaPair();
            pair.setId(Long.valueOf(i));
            pair.setPullWeight(1L);
            pair.setPushWeight(1L);

            DbDataMedia oracleMedia = getOracleMedia();
            oracleMedia.setId(tableId + i);
            pair.setSource(oracleMedia);

            DbDataMedia mysqlMedia = getMysqlMedia();
            mysqlMedia.setId(tableId + i + count);
            pair.setTarget(mysqlMedia);

            pairs.add(pair);
        }
        return pairs;
    }
}
