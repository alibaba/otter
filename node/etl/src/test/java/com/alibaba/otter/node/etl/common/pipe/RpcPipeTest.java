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

import mockit.Mock;
import mockit.Mockit;

import org.testng.annotations.Test;

import com.alibaba.otter.node.common.communication.NodeCommmunicationClient;
import com.alibaba.otter.node.etl.BaseOtterTest;
import com.alibaba.otter.node.etl.TestUtils;
import com.alibaba.otter.node.etl.common.pipe.impl.rpc.RowDataRpcPipe;
import com.alibaba.otter.node.etl.common.pipe.impl.rpc.RpcPipeKey;
import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

public class RpcPipeTest extends BaseOtterTest {

    @Test
    public void test_ok() {
        final DbBatch source = new DbBatch();
        RowBatch rowBatch = new RowBatch();
        Identity identity = new Identity();
        identity.setChannelId(100L);
        identity.setPipelineId(100L);
        identity.setProcessId(100L);
        rowBatch.setIdentity(identity);
        source.setRowBatch(rowBatch);

        final RowDataRpcPipe pipe = new RowDataRpcPipe();
        try {
            pipe.afterPropertiesSet();
        } catch (Exception e) {
            want.fail();
        }
        Mockit.setUpMock(NodeCommmunicationClient.class, new Object() {

            @Mock
            public Object call(Long nid, final Event event) {
                try {
                    return TestUtils.invokeMethod(pipe, "onGet", event);
                } catch (Exception e) {
                    want.fail();
                }

                return null;
            }

        });

        Mockit.setUpMock(RowDataRpcPipe.class, new Object() {

            @Mock
            private Long getNid() {
                return 1L;
            }

        });
        pipe.setNodeCommmunicationClient(new NodeCommmunicationClient());
        RpcPipeKey key = pipe.put(source);
        DbBatch target = pipe.get(key);
        want.bool(source.getRowBatch().getIdentity().equals(target.getRowBatch().getIdentity())).is(true);// identify相等
    }

    @Test
    public void test_timeout() {

        final DbBatch source = new DbBatch();
        RowBatch rowBatch = new RowBatch();
        Identity identity = new Identity();
        identity.setChannelId(100L);
        identity.setPipelineId(100L);
        identity.setProcessId(100L);
        rowBatch.setIdentity(identity);
        source.setRowBatch(rowBatch);

        final RowDataRpcPipe pipe = new RowDataRpcPipe();
        pipe.setTimeout(1 * 1000L);// 1s后超时
        try {
            pipe.afterPropertiesSet();
        } catch (Exception e) {
            want.fail();
        }
        Mockit.setUpMock(NodeCommmunicationClient.class, new Object() {

            @Mock
            public Object call(Long nid, final Event event) {
                try {
                    return TestUtils.invokeMethod(pipe, "onGet", event);
                } catch (Exception e) {
                    want.fail();
                }

                return null;
            }

        });

        Mockit.setUpMock(RowDataRpcPipe.class, new Object() {

            @Mock
            private Long getNid() {
                return 1L;
            }

        });
        pipe.setNodeCommmunicationClient(new NodeCommmunicationClient());

        RpcPipeKey key = pipe.put(source);
        try {
            Thread.sleep(1500L);
        } catch (InterruptedException e) {
            want.fail();
        }
        DbBatch target = pipe.get(key);
        want.bool(target == null).is(true);// 返回结果为空
    }
}
