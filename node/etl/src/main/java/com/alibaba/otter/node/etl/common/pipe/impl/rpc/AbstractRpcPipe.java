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

package com.alibaba.otter.node.etl.common.pipe.impl.rpc;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.node.etl.common.pipe.Pipe;
import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.communication.core.model.EventType;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.google.common.collect.OtterMigrateMap;

/**
 * 基于rpc通讯的数据传递
 * 
 * <pre>
 * PUT：基于内存cache的临时存储
 * GET: 基于远程rpc请求的调用获取
 * </pre>
 * 
 * @author jianghang 2011-10-17 下午01:29:49
 * @version 4.0.0
 */
public abstract class AbstractRpcPipe<T, KEY extends RpcPipeKey> implements Pipe<T, KEY>, InitializingBean {

    protected Long                     timeout = 60 * 1000L; // 对应的超时时间,1分钟

    protected Map<RpcPipeKey, DbBatch> cache;

    public void afterPropertiesSet() throws Exception {
        cache = OtterMigrateMap.makeSoftValueMapWithTimeout(timeout, TimeUnit.MILLISECONDS);
    }

    // rpc get操作事件
    public static class RpcEvent extends Event {

        private static final long serialVersionUID = 810191575813164952L;

        public RpcEvent(EventType eventType){
            super(eventType);
        }

        public RpcPipeKey key;

        public RpcPipeKey getKey() {
            return key;
        }

        public void setKey(RpcPipeKey key) {
            this.key = key;
        }

    }

    // ============== setter / getter ===============

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}
