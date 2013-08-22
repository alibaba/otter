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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mockit.Mocked;

import org.apache.commons.io.FileUtils;
import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.BaseDbTest;
import com.alibaba.otter.node.etl.load.exception.LoadException;
import com.alibaba.otter.node.etl.load.loader.db.FileLoadAction;
import com.alibaba.otter.node.etl.load.loader.db.context.FileLoadContext;
import com.alibaba.otter.node.etl.load.loader.weight.WeightController;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.Identity;

/**
 * @author zebinxu 2012-5-3 上午11:17:22
 */
public class FileLoadActionTest extends BaseDbTest {

    private static final int    NUMBER_OF_FILE_DATA_COPIES = 5;
    private static final long   TABLE_ID                   = 1L;
    private static final File   ROOT_DIR                   = new File(System.getProperty("java.io.tmpdir")
                                                                      + "/file_load_action_test");

    @SpringBeanByName
    private FileLoadAction      fileLoadAction;

    @SpringBeanByName
    @Mocked(realClassName = "com.alibaba.otter.node.common.config.impl.ConfigClientServiceImpl")
    private ConfigClientService configClientService;

    @Test
    public void testWithOutRootDir() throws Exception {
        File rootDir = new File("/null");
        Identity id = buildIdentity(1L, 2L, 3L);
        FileBatch fileBatch = buildFileBatch(id);
        fileBatch.getFiles().addAll(buildFileDatas("ns_", EventType.INSERT, 0, 20, false));

        try {
            fileLoadAction.load(fileBatch, rootDir, null);
        } catch (Exception e) {
            // expect for LoadException
            if (e instanceof LoadException) {
                return;
            }

            throw e;
        }

        want.fail("unreachable code.");

    }

    @Test
    public void testLoadWithLocal() throws IOException {

        // 构造fileData使用的参数，fileDataStartIndex 决定着 pipeline 与 fileData 对应的关系（通过
        // dataMediaPair 的 id），
        // 以及 dataMediaPair 的 pushWeight
        final int fileDataStartIndex = 0;
        final int fileDataCount = 50;
        final Pipeline pipeline = buildPipeline(fileDataStartIndex, fileDataCount);

        final Channel channel = new Channel();

        new NonStrictExpectations() {

            {
                configClientService.findChannel(anyLong);
                returns(channel);
                configClientService.findPipeline(anyLong);
                returns(pipeline);

            }
        };

        Identity id = buildIdentity(1L, 2L, 3L);
        FileBatch fileBatch = buildFileBatch(id);
        fileBatch.getFiles().addAll(buildFileDatas(null, EventType.INSERT, fileDataStartIndex, fileDataCount, true));

        WeightController controller = new WeightController(1);
        FileLoadContext context = fileLoadAction.load(fileBatch, ROOT_DIR, controller);
        want.object(context.getChannel()).isEqualTo(channel);
        want.object(context.getPipeline()).isEqualTo(pipeline);
        want.object(context.getPrepareDatas()).isEqualTo(fileBatch.getFiles());
        want.number(context.getProcessedDatas().size()).isEqualTo(fileBatch.getFiles().size());
    }

    @BeforeMethod
    public void setUp() throws IOException {
        FileUtils.deleteDirectory(ROOT_DIR);
    }

    @AfterMethod
    public void cleanUp() throws IOException {
        FileUtils.deleteDirectory(ROOT_DIR);
    }

    protected Pipeline buildPipeline(final int fileDataStartIndex, int fileDataCount) {
        final Pipeline pipeline = new Pipeline();
        pipeline.setParameters(new PipelineParameter());

        int dataMediaPairCount = fileDataCount / NUMBER_OF_FILE_DATA_COPIES;
        pipeline.setPairs(new ArrayList<DataMediaPair>(dataMediaPairCount));
        for (int i = fileDataStartIndex; i < dataMediaPairCount; i++) {
            DataMediaPair dataMediaPair = buildDataMediaPair(i, i);
            pipeline.getPairs().add(dataMediaPair);
        }
        return pipeline;
    }

    protected DataMediaPair buildDataMediaPair(long id, long pushWeight) {
        DataMediaPair result = new DataMediaPair();
        result.setId(id);
        result.setPushWeight(pushWeight);
        return result;
    }

    protected FileBatch buildFileBatch(Identity identity) {
        FileBatch fileBatch = new FileBatch();
        fileBatch.setIdentity(identity);
        return fileBatch;
    }

    protected List<FileData> buildFileDatas(String namespace, EventType eventType, int start, int count, boolean create)
                                                                                                                        throws IOException {
        List<FileData> files = new ArrayList<FileData>();

        for (int i = start; i < count; i++) {
            FileData fileData = new FileData();
            fileData.setNameSpace(namespace); // namespace is null means file is
                                              // local file
            fileData.setEventType(eventType);
            fileData.setPairId(i % NUMBER_OF_FILE_DATA_COPIES);
            fileData.setPath(ROOT_DIR.getAbsolutePath() + "/target/" + eventType.getValue() + i);

            String parentPath = ROOT_DIR.getPath();
            if (namespace != null) {
                parentPath = parentPath + "/" + namespace;
            }
            File file = new File(parentPath, fileData.getPath());
            if (!file.exists() && create) {
                FileUtils.touch(file);
            }

            fileData.setSize(file.exists() ? file.length() : 0);
            fileData.setLastModifiedTime(file.exists() ? file.lastModified() : Calendar.getInstance().getTimeInMillis());
            fileData.setTableId(TABLE_ID);

            files.add(fileData);
        }

        return files;
    }

    protected Identity buildIdentity(long channelId, long pipelineId, long processId) {
        Identity identity = new Identity();
        identity.setChannelId(channelId);
        identity.setPipelineId(pipelineId);
        identity.setProcessId(processId);
        return identity;
    }

}
