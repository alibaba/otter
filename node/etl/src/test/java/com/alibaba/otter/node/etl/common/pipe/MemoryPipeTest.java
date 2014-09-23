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

import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.common.pipe.impl.memory.MemoryPipeKey;
import com.alibaba.otter.node.etl.common.pipe.impl.memory.RowDataMemoryPipe;
import com.alibaba.otter.node.etl.BaseOtterTest;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

public class MemoryPipeTest extends BaseOtterTest {

    private static final String tmp = System.getProperty("java.io.tmpdir", "/tmp");

    @Test
    public void test_ok() {
        RowDataMemoryPipe pipe = new RowDataMemoryPipe();
        pipe.setDownloadDir(tmp);
        try {
            pipe.afterPropertiesSet();
        } catch (Exception e) {
            want.fail();
        }

        DbBatch source = new DbBatch();
        RowBatch rowBatch = new RowBatch();
        Identity identity = new Identity();
        identity.setChannelId(100L);
        identity.setPipelineId(100L);
        identity.setProcessId(100L);
        rowBatch.setIdentity(identity);
        source.setRowBatch(rowBatch);

        MemoryPipeKey key = pipe.put(source);
        DbBatch target = pipe.get(key);
        want.bool(source == target).is(true);// 引用为同一个
    }

    @Test
    public void test_timeout() {
        RowDataMemoryPipe pipe = new RowDataMemoryPipe();
        pipe.setTimeout(1 * 1000L);// 1s后超时
        pipe.setDownloadDir(tmp);
        try {
            pipe.afterPropertiesSet();
        } catch (Exception e) {
            want.fail();
        }

        DbBatch source = new DbBatch();
        RowBatch rowBatch = new RowBatch();
        Identity identity = new Identity();
        identity.setChannelId(100L);
        identity.setPipelineId(100L);
        identity.setProcessId(100L);
        rowBatch.setIdentity(identity);
        source.setRowBatch(rowBatch);

        MemoryPipeKey key = pipe.put(source);
        try {
            Thread.sleep(1500L);
        } catch (InterruptedException e) {
            want.fail();
        }
        DbBatch target = pipe.get(key);
        want.bool(target == null).is(true);// 返回结果为空
    }
}
