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

import java.sql.Types;
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
import com.alibaba.otter.node.etl.extract.extractor.DatabaseExtractor;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncConsistency;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncMode;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.db.DbDataMedia;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

public class DatabaseExtractorTest extends BaseDbTest {

    @SpringBeanByName
    private DatabaseExtractor   databaseExtractor;

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
    public void test_global_row() {
        final Pipeline pipeline = new Pipeline();
        pipeline.setId(100L);
        pipeline.getParameters().setSyncMode(SyncMode.ROW);
        pipeline.getParameters().setSyncConsistency(SyncConsistency.MEDIA);// 设置为全局

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
                eventData.setSchemaName("srf");
                eventData.setTableName("columns");
                rowBatch.merge(eventData);
            }
        }

        databaseExtractor.extract(new DbBatch(rowBatch));
        want.number(rowBatch.getDatas().size()).isEqualTo(count);
    }

    public void test_override_field() {
        final Pipeline pipeline = new Pipeline();
        pipeline.setId(100L);
        pipeline.getParameters().setSyncMode(SyncMode.FIELD);
        pipeline.getParameters().setSyncConsistency(SyncConsistency.BASE);// 设置为全局

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
                eventData.setSchemaName("srf");
                eventData.setTableName("columns");
                eventData.setSyncConsistency(SyncConsistency.MEDIA);
                rowBatch.merge(eventData);
            }
        }

        databaseExtractor.extract(new DbBatch(rowBatch));

        want.number(rowBatch.getDatas().size()).isEqualTo(count);
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
        List<EventColumn> result = new ArrayList<EventColumn>();
        result.add(buildColumn("id", Types.INTEGER, "" + value, true, false));
        result.add(buildColumn("name", Types.VARCHAR, "ljh_" + value, true, false));

        result.add(buildColumn("alias_name", Types.CHAR, "hello_" + value, false, false));
        result.add(buildColumn("amount", Types.DECIMAL, "100.01", false, false));
        result.add(buildColumn("text_b", Types.BLOB, "[116,101,120,116,95,98]", false, false));
        result.add(buildColumn("text_c", Types.CLOB, "中文", false, false));
        result.add(buildColumn("curr_date", Types.DATE, "2011-01-01", false, false));
        result.add(buildColumn("gmt_create", Types.TIMESTAMP, "2011-01-01 11:11:11", false, false));
        result.add(buildColumn("gmt_modify", Types.TIMESTAMP, "2011-01-01 11:11:11", false, false));
        return result;
    }

    private EventColumn buildColumn(String name, int type, String value, boolean isKey, boolean isNull) {
        EventColumn column = new EventColumn();
        column.setColumnName(name);
        column.setColumnType(type);
        column.setColumnValue(value);
        column.setKey(isKey);
        column.setNull(isNull);
        return column;
    }

}
