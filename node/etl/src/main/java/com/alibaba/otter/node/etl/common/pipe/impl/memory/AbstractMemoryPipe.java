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

package com.alibaba.otter.node.etl.common.pipe.impl.memory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.node.etl.common.pipe.Pipe;
import com.alibaba.otter.shared.etl.model.DbBatch;
import com.google.common.collect.OtterMigrateMap;

/**
 * 基于内存版本的pipe实现
 * 
 * @author jianghang 2011-10-13 下午05:36:33
 * @version 4.0.0
 */
public abstract class AbstractMemoryPipe<T, KEY extends MemoryPipeKey> implements Pipe<T, KEY>, InitializingBean {

    protected Long                        timeout = 60 * 1000L; // 对应的超时时间,1分钟

    protected Map<MemoryPipeKey, DbBatch> cache;

    public void afterPropertiesSet() throws Exception {
        // 一定要设置过期时间，因为针对rollback操作，不会有后续的节点来获取数据，需要自动过期删除掉
        cache = OtterMigrateMap.makeSoftValueMapWithTimeout(timeout, TimeUnit.MILLISECONDS);
    }

    // ============== setter / getter ===============

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

}
