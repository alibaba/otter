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

package com.alibaba.otter.node.etl.load;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mockit.Mocked;

import org.jtester.annotations.SpringBeanFrom;
import org.jtester.core.TestedObject;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.BaseDbTest;
import com.alibaba.otter.node.etl.load.loader.db.DbLoadAction;
import com.alibaba.otter.node.etl.load.loader.weight.WeightController;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigRegistry;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.db.DbDataMedia;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

public class DbLoadActionTest extends BaseDbTest {

    private DbLoadAction        dbLoadAction;

    @SpringBeanFrom
    @Mocked
    private ConfigClientService configClientService;

    @Test
    public void test_db_load_oracle() {
        ArbitrateConfigRegistry.regist(configClientService);
        dbLoadAction = (DbLoadAction) TestedObject.getSpringBeanFactory().getBean("dbLoadAction");

        final Channel channel = new Channel();
        channel.setId(1L);

        final Pipeline pipeline = new Pipeline();
        pipeline.setId(100L);
        List<DataMediaPair> pairs = generatorDataMediaPairForOracle(20);
        pipeline.setPairs(pairs);
        pipeline.getParameters().merge(new SystemParameter());
        pipeline.getParameters().merge(new ChannelParameter());

        // final Pipeline oppositePipeline = new Pipeline();
        // oppositePipeline.setId(101L);
        channel.setPipelines(Arrays.asList(pipeline));

        final Node currentNode = new Node();
        currentNode.setId(1L);
        new NonStrictExpectations() {

            {
                configClientService.findChannel(anyLong);
                returns(channel);
                configClientService.findPipeline(anyLong);
                returns(pipeline);
                configClientService.currentNode();
                returns(currentNode);
            }
        };

        Identity identity = new Identity();
        identity.setChannelId(100L);
        identity.setPipelineId(100L);
        identity.setProcessId(100L);

        RowBatch rowBatch = new RowBatch();
        rowBatch.setIdentity(identity);
        List<EventData> eventDatas = generatorEventDataForOracle(0, 20, EventType.INSERT);
        for (EventData eventData : eventDatas) {
            rowBatch.merge(eventData);
        }
        eventDatas = generatorEventDataForOracle(10, 10, EventType.INSERT);
        for (EventData eventData : eventDatas) {
            rowBatch.merge(eventData);
        }
        eventDatas = generatorEventDataForOracle(19, 1, EventType.DELETE);
        for (EventData eventData : eventDatas) {
            rowBatch.merge(eventData);
        }

        WeightController controller = new WeightController(1);
        dbLoadAction.load(rowBatch, controller);
    }

    @Test
    public void test_db_load_mysql() {
        ArbitrateConfigRegistry.regist(configClientService);
        dbLoadAction = (DbLoadAction) TestedObject.getSpringBeanFactory().getBean("dbLoadAction");

        final Channel channel = new Channel();
        channel.setId(1L);

        final Pipeline pipeline = new Pipeline();
        pipeline.setId(100L);
        List<DataMediaPair> pairs = generatorDataMediaPairForMysql(20);
        pipeline.setPairs(pairs);
        pipeline.getParameters().merge(new SystemParameter());
        pipeline.getParameters().merge(new ChannelParameter());
        // pipeline.getParameters().setChannelInfo("LJH_DEMO");

        // final Pipeline oppositePipeline = new Pipeline();
        // oppositePipeline.setId(101L);
        channel.setPipelines(Arrays.asList(pipeline));

        final Node currentNode = new Node();
        currentNode.setId(1L);
        new NonStrictExpectations() {

            {
                configClientService.findChannel(anyLong);
                returns(channel);
                configClientService.findPipeline(anyLong);
                returns(pipeline);
                configClientService.currentNode();
                returns(currentNode);
            }
        };

        Identity identity = new Identity();
        identity.setChannelId(100L);
        identity.setPipelineId(100L);
        identity.setProcessId(100L);

        RowBatch rowBatch = new RowBatch();
        rowBatch.setIdentity(identity);
        List<EventData> eventDatas = generatorEventDataForMysql(0, 20, EventType.INSERT);
        for (EventData eventData : eventDatas) {
            rowBatch.merge(eventData);
        }
        eventDatas = generatorEventDataForMysql(10, 10, EventType.INSERT);
        for (EventData eventData : eventDatas) {
            rowBatch.merge(eventData);
        }
        eventDatas = generatorEventDataForMysql(19, 1, EventType.DELETE);
        for (EventData eventData : eventDatas) {
            rowBatch.merge(eventData);
        }

        WeightController controller = new WeightController(1);
        dbLoadAction.load(rowBatch, controller);
    }

    private List<EventData> generatorEventDataForOracle(int start, int count, EventType type) {
        List<EventData> eventDatas = new ArrayList<EventData>();
        for (int i = 0; i < count; i++) {
            int index = i + 1 + start;
            EventData eventData = new EventData();
            eventData.setPairId(index);
            eventData.setTableId(1L);
            eventData.setSchemaName("srf");
            eventData.setTableName("columns");
            eventData.setEventType(type);
            eventData.setExecuteTime(100L);
            eventData.getKeys().add(buildColumn("id", Types.NUMERIC, "" + index, true, false));
            eventData.getKeys().add(buildColumn("name", Types.VARCHAR, "ljh_" + index, true, false));
            eventData.getOldKeys().add(buildColumn("id", Types.NUMERIC, "" + index, true, false));
            eventData.getOldKeys().add(buildColumn("name", Types.VARCHAR, "ljh_" + index, true, false));

            eventData.getColumns().add(buildColumn("alias_name", Types.CHAR, "hello_" + index, false, false));
            eventData.getColumns().add(buildColumn("amount", Types.NUMERIC, "100.01", false, false));
            eventData.getColumns().add(buildColumn("text_b", Types.BLOB, "[116,101,120,116,95,98]", false, false));
            eventData.getColumns().add(buildColumn("text_c", Types.CLOB, "中文", false, false));
            eventData.getColumns().add(buildColumn("curr_date", Types.DATE, "2011-01-01", false, false));
            eventData.getColumns().add(buildColumn("gmt_create", Types.DATE, "2011-01-01 11:11:11", false, false));
            eventData.getColumns().add(buildColumn("gmt_modify", Types.DATE, "2011-01-01 11:11:11", false, false));

            // OracleSqlTemplate sqlTemplate = new OracleSqlTemplate();
            // String sql = null;
            // if (type.isInsert()) {
            // sql = sqlTemplate.getMergeSql(eventData.getSchemaName(),
            // eventData.getTableName(),
            // buildColumnNames(eventData.getKeys()),
            // buildColumnNames(eventData.getColumns()), new String[] {});
            // } else if (type.isUpdate()) {
            // sql = sqlTemplate.getUpdateSql(eventData.getSchemaName(),
            // eventData.getTableName(),
            // buildColumnNames(eventData.getKeys()),
            // buildColumnNames(eventData.getColumns()));
            // } else if (type.isDelete()) {
            // sql = sqlTemplate.getDeleteSql(eventData.getSchemaName(),
            // eventData.getTableName(),
            // buildColumnNames(eventData.getKeys()));
            // }
            // eventData.setSql(sql);

            eventDatas.add(eventData);
        }

        return eventDatas;
    }

    private List<EventData> generatorEventDataForMysql(int start, int count, EventType type) {
        List<EventData> eventDatas = new ArrayList<EventData>();
        for (int i = 0; i < count; i++) {
            int index = i + 1 + start;
            EventData eventData = new EventData();
            eventData.setPairId(index);
            eventData.setTableId(1L);
            eventData.setSchemaName("srf");
            eventData.setTableName("columns");
            eventData.setEventType(type);
            eventData.setExecuteTime(100L);
            eventData.getKeys().add(buildColumn("id", Types.INTEGER, "" + index, true, false));
            eventData.getKeys().add(buildColumn("name", Types.VARCHAR, "ljh_" + index, true, false));

            eventData.getOldKeys().add(buildColumn("id", Types.INTEGER, "" + index, true, false));
            eventData.getOldKeys().add(buildColumn("name", Types.VARCHAR, "ljh_" + index, true, false));

            eventData.getColumns().add(buildColumn("alias_name", Types.CHAR, "hello_" + index, false, false));
            eventData.getColumns().add(buildColumn("amount", Types.DECIMAL, "100.01", false, false));
            eventData.getColumns().add(buildColumn("text_b", Types.BLOB, "[116,101,120,116,95,98]", false, false));
            eventData.getColumns().add(buildColumn("text_c", Types.CLOB, "中文", false, false));
            eventData.getColumns().add(buildColumn("curr_date", Types.DATE, "2011-01-01", false, false));
            eventData.getColumns().add(buildColumn("gmt_create", Types.TIMESTAMP, "2011-01-01 11:11:11", false, false));
            eventData.getColumns().add(buildColumn("gmt_modify", Types.TIMESTAMP, "2011-01-01 11:11:11", false, false));

            // MysqlSqlTemplate sqlTemplate = new MysqlSqlTemplate();
            // String sql = null;
            // if (type.isInsert()) {
            // sql = sqlTemplate.getMergeSql(eventData.getSchemaName(),
            // eventData.getTableName(),
            // buildColumnNames(eventData.getKeys()),
            // buildColumnNames(eventData.getColumns()), new String[] {});
            // } else if (type.isUpdate()) {
            // sql = sqlTemplate.getUpdateSql(eventData.getSchemaName(),
            // eventData.getTableName(),
            // buildColumnNames(eventData.getKeys()),
            // buildColumnNames(eventData.getColumns()));
            // } else if (type.isDelete()) {
            // sql = sqlTemplate.getDeleteSql(eventData.getSchemaName(),
            // eventData.getTableName(),
            // buildColumnNames(eventData.getKeys()));
            // }
            // eventData.setSql(sql);
            eventDatas.add(eventData);
        }

        return eventDatas;
    }

    // private String[] buildColumnNames(List<EventColumn> columns) {
    // String[] result = new String[columns.size()];
    // for (int i = 0; i < columns.size(); i++) {
    // EventColumn column = columns.get(i);
    // result[i] = column.getColumnName();
    // }
    // return result;
    // }

    private EventColumn buildColumn(String name, int type, String value, boolean isKey, boolean isNull) {
        EventColumn column = new EventColumn();
        column.setColumnName(name);
        column.setColumnType(type);
        column.setColumnValue(value);
        column.setKey(isKey);
        column.setNull(isNull);
        return column;
    }

    private List<DataMediaPair> generatorDataMediaPairForOracle(int count) {
        List<DataMediaPair> pairs = new ArrayList<DataMediaPair>();
        for (int i = 0; i < count; i++) {
            DataMediaPair pair = new DataMediaPair();
            int index = i + 1;
            pair.setId(Long.valueOf(index));
            pair.setPullWeight(count - Long.valueOf(index));
            pair.setPushWeight(count - Long.valueOf(index));

            DbDataMedia mysqlMedia = getMysqlMedia();
            mysqlMedia.setId(2L);
            pair.setSource(mysqlMedia);

            DbDataMedia oracleMedia = getOracleMedia();
            oracleMedia.setId(1L);
            pair.setTarget(oracleMedia);
            pairs.add(pair);
        }
        return pairs;
    }

    private List<DataMediaPair> generatorDataMediaPairForMysql(int count) {
        List<DataMediaPair> pairs = new ArrayList<DataMediaPair>();
        for (int i = 0; i < count; i++) {
            DataMediaPair pair = new DataMediaPair();
            int index = i + 1;
            pair.setId(Long.valueOf(index));
            pair.setPullWeight(count - Long.valueOf(index));
            pair.setPushWeight(count - Long.valueOf(index));

            DbDataMedia oracleMedia = getOracleMedia();
            oracleMedia.setId(2L);
            pair.setSource(oracleMedia);

            DbDataMedia mysqlMedia = getMysqlMedia();
            mysqlMedia.setId(1L);
            pair.setTarget(mysqlMedia);
            pairs.add(pair);
        }
        return pairs;
    }
}
