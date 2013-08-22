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

import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.common.io.download.DataRetriever;
import com.alibaba.otter.node.etl.common.io.download.exception.DataRetrieveException;
import com.alibaba.otter.node.etl.common.io.download.impl.aria2c.Aria2cRetriever;
import com.alibaba.otter.node.etl.BaseOtterTest;

/**
 * @author jianghang 2011-10-10 下午06:23:33
 * @version 4.0.0
 */
public class Aria2cDownLoadIntegration extends BaseOtterTest {

    private static final String tmp = System.getProperty("java.io.tmpdir", "/tmp");

    @Test
    public void testDownLoad_ok() {
        DataRetriever retriever = new Aria2cRetriever("http://china.alibaba.com", tmp);
        try {
            retriever.connect();
            retriever.doRetrieve();
        } catch (DataRetrieveException ex) {
            retriever.abort();
        } finally {
            retriever.disconnect();
        }
    }

    @Test
    public void testDownLoad_failed() {
        DataRetriever retriever = new Aria2cRetriever("aaaaaaa/sssss", tmp);
        try {
            retriever.connect();
            retriever.doRetrieve();
        } catch (DataRetrieveException ex) {
            retriever.abort();
        } finally {
            retriever.disconnect();
        }
    }
}
