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

package com.alibaba.otter.node.etl.transform;

import java.io.File;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mockit.Mocked;

import org.jtester.annotations.SpringBeanByName;
import org.jtester.annotations.SpringBeanFrom;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.BaseDbTest;
import com.alibaba.otter.node.etl.transform.transformer.OtterTransformerFactory;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncMode;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.db.DbDataMedia;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter;
import com.alibaba.otter.shared.etl.model.BatchObject;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

class OtterTransformerTest extends BaseDbTest {

    @SpringBeanFrom
    @Mocked
    private ConfigClientService     configClientService;

    @SpringBeanByName
    private OtterTransformerFactory otterTransformFactory;

    @Test
    public void test_rowData_mysql_oracle() {
        final Pipeline pipeline = new Pipeline();
        pipeline.setId(100L);

        List<DataMediaPair> pairs = new ArrayList<DataMediaPair>();
        DataMediaPair pair1 = new DataMediaPair();
        pair1.setId(1L);
        pair1.setPipelineId(pipeline.getId());
        pair1.setPullWeight(1L);
        pair1.setPushWeight(1L);

        DbDataMedia mysqlMedia = getMysqlMedia();
        mysqlMedia.setId(1L);
        pair1.setSource(mysqlMedia);

        DbDataMedia oracleMedia = getOracleMedia();
        pair1.setTarget(oracleMedia);
        pairs.add(pair1);
        pipeline.setPairs(pairs);
        PipelineParameter param = new PipelineParameter();
        param.setSyncMode(SyncMode.ROW);

        pipeline.setParameters(param);
        new NonStrictExpectations() {

            {
                configClientService.findPipeline(anyLong);
                returns(pipeline);
            }
        };

        Identity identity = new Identity();
        identity.setChannelId(100L);
        identity.setPipelineId(100L);
        identity.setProcessId(100L);

        RowBatch rowBatch = new RowBatch();
        rowBatch.setIdentity(identity);
        EventData eventData = new EventData();
        eventData.setTableId(1L);
        eventData.setSchemaName("srf");
        eventData.setTableName("columns");
        eventData.setEventType(EventType.UPDATE);
        eventData.setExecuteTime(100L);

        eventData.getKeys().add(buildColumn("id", Types.INTEGER, "1", true, false));
        eventData.getKeys().add(buildColumn("name", Types.VARCHAR, "ljh", true, false));

        eventData.getColumns().add(buildColumn("alias_name", Types.CHAR, "hello", false, false));
        eventData.getColumns().add(buildColumn("amount", Types.DECIMAL, "100.01", false, false));
        eventData.getColumns().add(buildColumn("text_b", Types.BLOB, "[116,101,120,116,95,98]", false, false));
        eventData.getColumns().add(buildColumn("text_c", Types.CLOB, "text_c", false, false));
        eventData.getColumns().add(buildColumn("curr_date", Types.DATE, "2011-01-01", false, false));
        eventData.getColumns().add(buildColumn("gmt_create", Types.TIMESTAMP, "2011-01-01 11:11:11", false, false));
        eventData.getColumns().add(buildColumn("gmt_modify", Types.TIMESTAMP, "2011-01-01 11:11:11", false, false));

        rowBatch.merge(eventData);

        Map<Class, BatchObject> batchs = otterTransformFactory.transform(rowBatch);
        RowBatch result = (RowBatch) batchs.get(EventData.class);
        want.number(result.getDatas().size()).isEqualTo(1);
    }

    @Test
    public void test_rowData_oracle_mysql() {
        final Pipeline pipeline = new Pipeline();
        pipeline.setId(100L);

        List<DataMediaPair> pairs = new ArrayList<DataMediaPair>();
        DataMediaPair pair1 = new DataMediaPair();
        pair1.setId(1L);
        pair1.setPipelineId(pipeline.getId());
        pair1.setPullWeight(1L);
        pair1.setPushWeight(1L);

        DbDataMedia oracleMedia = getOracleMedia();
        oracleMedia.setId(1L);
        pair1.setSource(oracleMedia);

        DbDataMedia mysqlMedia = getMysqlMedia();
        pair1.setTarget(mysqlMedia);

        pairs.add(pair1);
        pipeline.setPairs(pairs);
        PipelineParameter param = new PipelineParameter();
        param.setSyncMode(SyncMode.ROW);

        pipeline.setParameters(param);
        new NonStrictExpectations() {

            {
                configClientService.findPipeline(anyLong);
                returns(pipeline);
            }
        };

        Identity identity = new Identity();
        identity.setChannelId(100L);
        identity.setPipelineId(100L);
        identity.setProcessId(100L);

        RowBatch rowBatch = new RowBatch();
        rowBatch.setIdentity(identity);
        EventData eventData = new EventData();
        eventData.setTableId(1L);
        eventData.setSchemaName("srf");
        eventData.setTableName("columns");
        eventData.setEventType(EventType.UPDATE);
        eventData.setExecuteTime(100L);

        eventData.getKeys().add(buildColumn("id", Types.NUMERIC, "1", true, false));
        eventData.getKeys().add(buildColumn("name", Types.VARCHAR, "ljh", true, false));

        eventData.getColumns().add(buildColumn("alias_name", Types.CHAR, "hello", false, false));
        eventData.getColumns().add(buildColumn("amount", Types.NUMERIC, "100.01", false, false));
        eventData.getColumns().add(buildColumn("text_b", Types.BLOB, "[116,101,120,116,95,98]", false, false));
        eventData.getColumns().add(buildColumn("text_c", Types.CLOB, "text_c", false, false));
        eventData.getColumns().add(buildColumn("curr_date", Types.DATE, "2011-01-01", false, false));
        eventData.getColumns().add(buildColumn("gmt_create", Types.DATE, "2011-01-01 11:11:11", false, false));
        eventData.getColumns().add(buildColumn("gmt_modify", Types.DATE, "2011-01-01 11:11:11", false, false));

        rowBatch.merge(eventData);

        Map<Class, BatchObject> batchs = otterTransformFactory.transform(rowBatch);
        RowBatch result = (RowBatch) batchs.get(EventData.class);
        want.number(result.getDatas().size()).isEqualTo(1);
    }

    @Test
    public void test_fileData() {
        final Pipeline pipeline = new Pipeline();
        pipeline.setId(100L);

        List<DataMediaPair> pairs = new ArrayList<DataMediaPair>();
        DataMediaPair pair1 = new DataMediaPair();
        pair1.setId(1L);
        pair1.setPipelineId(pipeline.getId());
        pair1.setPullWeight(1L);
        pair1.setPushWeight(1L);

        DbDataMedia oracleMedia = getOracleMedia();
        oracleMedia.setId(1L);
        pair1.setSource(oracleMedia);

        DbDataMedia mysqlMedia = getMysqlMedia();
        pair1.setTarget(mysqlMedia);

        pairs.add(pair1);
        pipeline.setPairs(pairs);
        new NonStrictExpectations() {

            {
                configClientService.findPipeline(anyLong);
                returns(pipeline);
            }
        };

        Identity identity = new Identity();
        identity.setChannelId(100L);
        identity.setPipelineId(100L);
        identity.setProcessId(100L);

        FileBatch fileBatch = new FileBatch();
        fileBatch.setIdentity(identity);
        File localFile = new File("/tmp", "httpPipeTest.jpg");
        FileData localFileData = new FileData();
        localFileData.setTableId(1L);
        localFileData.setPairId(1L);
        localFileData.setPath(localFile.getPath());
        fileBatch.getFiles().add(localFileData);

        Map<Class, BatchObject> batchs = otterTransformFactory.transform(fileBatch);
        FileBatch result = (FileBatch) batchs.get(FileData.class);
        want.number(result.getFiles().size()).isEqualTo(1);
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
