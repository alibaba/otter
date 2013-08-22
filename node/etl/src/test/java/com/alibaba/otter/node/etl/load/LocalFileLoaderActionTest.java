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
import java.util.Date;
import java.util.List;

import mockit.Mocked;

import org.apache.commons.lang.math.RandomUtils;
import org.jtester.annotations.SpringBeanByName;
import org.jtester.annotations.SpringBeanFrom;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.BaseDbTest;
import com.alibaba.otter.node.etl.load.loader.db.FileLoadAction;
import com.alibaba.otter.node.etl.load.loader.weight.WeightController;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.db.DbDataMedia;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.NioUtils;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.Identity;

class LocalFileLoaderActionTest extends BaseDbTest {

    private static final String OTTERLOAD = "otterload";

    private static final String tmp       = System.getProperty("java.io.tmpdir", "/tmp");

    @SpringBeanByName
    private FileLoadAction      fileLoadAction;

    @SpringBeanFrom
    @Mocked
    private ConfigClientService configClientService;

    @Test
    public void test_load_file() {
        final Pipeline pipeline = new Pipeline();
        pipeline.setId(100L);
        List<DataMediaPair> pairs = generatorDataMediaPair(10);
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
        fileBatch.getFiles().addAll(generatorLocalFileData("fileLoad", 10));

        WeightController controller = new WeightController(1);
        fileLoadAction.load(fileBatch, new File(tmp + File.separator + OTTERLOAD), controller);

        File target = new File(tmp + File.separator + OTTERLOAD + "_loaded/");
        want.number(target.listFiles().length).isEqualTo(10);
        NioUtils.delete(target);
    }

    private List<DataMediaPair> generatorDataMediaPair(int count) {
        List<DataMediaPair> pairs = new ArrayList<DataMediaPair>();
        for (int i = 0; i < count; i++) {
            DataMediaPair pair = new DataMediaPair();
            pair.setId(Long.valueOf(i));
            pair.setPullWeight(Long.valueOf(i));
            pair.setPushWeight(Long.valueOf(i));

            DbDataMedia mysqlMedia = getMysqlMedia();
            mysqlMedia.setId(Long.valueOf(count + 1));
            pair.setSource(mysqlMedia);

            DbDataMedia oracleMedia = getOracleMedia();
            oracleMedia.setId(Long.valueOf(i));
            pair.setTarget(oracleMedia);
            pairs.add(pair);
        }
        return pairs;
    }

    private List<FileData> generatorLocalFileData(String prefix, int count) {
        List<FileData> result = new ArrayList<FileData>();
        String target = tmp + File.separator + OTTERLOAD + "_loaded/";
        for (int i = 0; i < count; i++) {
            String filepath = tmp + File.separator + OTTERLOAD + target;
            File local = new File(filepath, prefix + "_" + i + ".jpg");
            FileData localFileData = new FileData();
            localFileData.setPairId(i);
            localFileData.setTableId(i);
            localFileData.setPath(target + local.getName());
            localFileData.setLastModifiedTime(new Date().getTime());
            try {
                byte[] data = getBlock((i + 1) * 1024);
                localFileData.setSize(data.length);
                NioUtils.write(data, local);
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
