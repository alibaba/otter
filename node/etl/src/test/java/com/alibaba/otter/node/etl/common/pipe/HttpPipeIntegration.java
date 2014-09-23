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

package com.alibaba.otter.node.etl.common.pipe;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import mockit.Mock;
import mockit.Mocked;
import mockit.Mockit;

import org.apache.commons.lang.math.RandomUtils;
import org.jtester.annotations.SpringBeanByName;
import org.jtester.annotations.SpringBeanFrom;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.BaseOtterTest;
import com.alibaba.otter.node.etl.common.jetty.JettyEmbedServer;
import com.alibaba.otter.node.etl.common.pipe.impl.http.AttachmentHttpPipe;
import com.alibaba.otter.node.etl.common.pipe.impl.http.HttpPipeKey;
import com.alibaba.otter.node.etl.common.pipe.impl.http.RowDataHttpPipe;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.node.NodeParameter;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter.RetrieverType;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.NioUtils;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

public class HttpPipeIntegration extends BaseOtterTest {

    private static final String tmp = System.getProperty("java.io.tmpdir", "/tmp");

    @SpringBeanByName
    private AttachmentHttpPipe  attachmentHttpPipe;

    @SpringBeanByName
    private RowDataHttpPipe     rowDataHttpPipe;

    @SpringBeanFrom
    @Mocked
    private ConfigClientService configClientService;

    @BeforeClass
    public void initial() {
        Mockit.setUpMock(JettyEmbedServer.class, new Object() {

            @Mock
            private Integer getPort() {
                return null;
            }

        });

    }

    @Test
    public void test_attachment() {
        final Node currentNode = new Node();
        currentNode.setId(1L);
        currentNode.setIp("127.0.0.1");
        currentNode.setParameters(new NodeParameter());
        final Pipeline pipeline = new Pipeline();
        pipeline.getParameters().setRetriever(RetrieverType.ARIA2C);
        // mock一下
        new NonStrictExpectations() {

            {
                configClientService.currentNode();
                returns(currentNode);

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
        File localFile = new File(tmp, "httpPipeTest.jpg");
        FileData localFileData = new FileData();
        localFileData.setEventType(EventType.INSERT);
        localFileData.setPath(localFile.getPath());
        fileBatch.getFiles().add(localFileData);
        try {
            byte[] data = getBlock(10 * 1024);
            NioUtils.write(data, localFile);
            HttpPipeKey key = attachmentHttpPipe.put(fileBatch);
            File target = attachmentHttpPipe.get(key);
            byte[] getbytes = NioUtils.read(new File(target, localFile.getPath()));
            check(data, getbytes);
        } catch (IOException e) {
            want.fail();
        } finally {
            NioUtils.delete(localFile);
        }

    }

    @Test
    public void test_rowData() {
        final Node currentNode = new Node();
        currentNode.setId(1L);
        currentNode.setIp("127.0.0.1");
        currentNode.setParameters(new NodeParameter());
        final Pipeline pipeline = new Pipeline();
        pipeline.getParameters().setRetriever(RetrieverType.ARIA2C);
        // mock一下
        new NonStrictExpectations() {

            {
                configClientService.currentNode();
                returns(currentNode);

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
        File localFile = new File(tmp, "httpPipeTest.jpg");
        FileData localFileData = new FileData();
        localFileData.setPath(localFile.getPath());
        localFileData.setEventType(EventType.INSERT);
        localFileData.setLastModifiedTime(new Date().getTime());
        localFileData.setSize(100L);
        localFileData.setTableId(1L);
        fileBatch.getFiles().add(localFileData);

        RowBatch rowBatch = new RowBatch();
        rowBatch.setIdentity(identity);
        EventData eventData = new EventData();
        eventData.setTableId(1L);
        eventData.setSchemaName("otter");
        eventData.setTableName("test");
        eventData.setEventType(EventType.INSERT);
        eventData.setExecuteTime(100L);

        EventColumn primaryKey = new EventColumn();
        primaryKey.setColumnName("id");
        primaryKey.setColumnType(1);
        primaryKey.setColumnValue("1");
        primaryKey.setKey(true);
        primaryKey.setNull(false);
        eventData.getKeys().add(primaryKey);

        EventColumn column = new EventColumn();
        column.setColumnName("name");
        column.setColumnType(1);
        column.setColumnValue("test");
        column.setKey(false);
        column.setNull(false);
        eventData.getColumns().add(column);

        rowBatch.merge(eventData);

        DbBatch dbBatch = new DbBatch();
        dbBatch.setRowBatch(rowBatch);
        dbBatch.setFileBatch(fileBatch);

        HttpPipeKey key = rowDataHttpPipe.put(dbBatch);
        DbBatch target = rowDataHttpPipe.get(key);

        want.bool(target.getRowBatch().getIdentity().equals(identity));
        want.object(target).notNull();
    }

    private void check(byte[] src, byte[] dest) {
        want.object(src).notNull();
        want.object(dest).notNull();
        want.bool(src.length == dest.length).is(true);

        for (int i = 0; i < src.length; i++) {
            if (src[i] != dest[i]) {
                want.fail();
            }
        }
    }

    private byte[] getBlock(int length) {
        byte[] rawData = new byte[length];
        for (int i = 0; i < rawData.length; i++) {
            rawData[i] = (byte) (' ' + RandomUtils.nextInt(95));

        }
        return rawData;
    }
}
