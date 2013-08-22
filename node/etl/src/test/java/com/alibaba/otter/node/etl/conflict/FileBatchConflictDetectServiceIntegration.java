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

package com.alibaba.otter.node.etl.conflict;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mockit.Mocked;

import org.apache.commons.lang.math.RandomUtils;
import org.jtester.annotations.SpringBeanByName;
import org.jtester.annotations.SpringBeanFrom;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.BaseOtterTest;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.NioUtils;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.Identity;

public class FileBatchConflictDetectServiceIntegration extends BaseOtterTest {

    private static final String            OTTERLOAD = "otterload";

    private static final String            tmp       = System.getProperty("java.io.tmpdir", "/tmp");

    @SpringBeanByName
    private FileBatchConflictDetectService fileBatchConflictDetectService;

    @SpringBeanFrom
    @Mocked
    private ConfigClientService            configClientService;

    @BeforeClass
    public void initial() {
        System.setProperty("nid", "1");
    }

    @Test
    public void test_localFile() {
        final Pipeline pipeline = new Pipeline();
        pipeline.setId(100L);

        final Node currentNode = new Node();
        currentNode.setId(1L);
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

        fileBatch.getFiles().addAll(generatorLocalFileData("fileLoad", 10));
        FileBatch result = fileBatchConflictDetectService.detect(fileBatch, 1L);
        want.number(result.getFiles().size()).isEqualTo(0);

        NioUtils.delete(new File(tmp + File.separator + OTTERLOAD));
    }

    private List<FileData> generatorLocalFileData(String prefix, int count) {
        List<FileData> result = new ArrayList<FileData>();
        for (int i = 0; i < count; i++) {
            String filepath = tmp + File.separator + OTTERLOAD + File.separator;
            File local = new File(filepath, prefix + "_" + i + ".jpg");
            FileData localFileData = new FileData();
            localFileData.setEventType(EventType.UPDATE);
            localFileData.setPairId(i);
            localFileData.setTableId(i);
            localFileData.setNameSpace(null);
            localFileData.setPath(local.getPath());
            try {
                byte[] data = getBlock((i + 1) * 1024);
                localFileData.setSize(data.length);
                NioUtils.write(data, local);
                localFileData.setLastModifiedTime(local.lastModified());
            } catch (IOException e) {
                want.fail();
            }
            result.add(localFileData);
        }

        return result;
    }

    private byte[] getBlock(int length) {
        byte[] rawData = new byte[length];
        for (int i = 0; i < rawData.length; i++) {
            rawData[i] = (byte) (' ' + RandomUtils.nextInt(95));

        }
        return rawData;
    }
}
