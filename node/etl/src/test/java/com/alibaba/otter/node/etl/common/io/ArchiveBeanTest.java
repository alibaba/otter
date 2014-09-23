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

package com.alibaba.otter.node.etl.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.BaseOtterTest;
import com.alibaba.otter.node.etl.common.pipe.impl.http.archive.ArchiveBean;
import com.alibaba.otter.node.etl.common.pipe.impl.http.archive.ArchiveRetriverCallback;
import com.alibaba.otter.shared.common.utils.NioUtils;
import com.alibaba.otter.shared.etl.model.FileData;

/**
 * 测试下压缩和解压缩
 * 
 * @author jianghang 2011-10-11 下午05:58:53
 * @version 4.0.0
 */
public class ArchiveBeanTest extends BaseOtterTest {

    private static final String tmp = System.getProperty("java.io.tmpdir", "/tmp");

    @Test
    public void test_simple() {
        File[] files = new File[10];

        List<FileData> fileDatas = new ArrayList<FileData>();
        File archiveFile = new File(tmp, "pack.zip");
        File unpack = new File(tmp, "unpack");

        ArchiveBean archiveBean = new ArchiveBean();
        try {
            archiveBean.afterPropertiesSet();
        } catch (Exception e1) {
            want.fail();
        }

        try {
            // for (int i = 0; i < 10; i++) {
            // files[i] = new File(tmp, "archiveTest_" + i + ".txt");
            // datas[i] = getBlock((i + 1) * 1024);
            // NioUtils.write(datas[i], files[i]);
            //
            // FileData filedata = new FileData();
            // filedata.setPath(files[i].getPath());
            // fileDatas.add(filedata);
            // }

            archiveBean.pack(archiveFile, fileDatas, new ArchiveRetriverCallback<FileData>() {

                public InputStream retrive(FileData source) {
                    try {
                        return new FileInputStream(new File(source.getPath()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        want.fail();
                    }
                    return null;
                }
            });

            // 开始解压
            List<File> result = archiveBean.unpack(archiveFile, unpack);
            want.bool(result.size() == fileDatas.size());

            // File dir = new File(unpack, archiveFile.getParent());
            // File[] unpackFiles = dir.listFiles();
            //
            // List<File> unpackFilesList = Arrays.asList(unpackFiles);
            // Collections.sort(unpackFilesList); // 排序一下
            // for (int i = 0; i < unpackFilesList.size(); i++) {
            // byte[] data = NioUtils.read(unpackFilesList.get(i));
            // check(data, datas[i]);
            // }

        } catch (Exception e) {
            want.fail();
        } finally {
            for (int i = 0; i < files.length; i++) {
                NioUtils.delete(files[i]);
            }
            NioUtils.delete(archiveFile);
            NioUtils.delete(unpack);
        }
    }

}
