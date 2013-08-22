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

package com.alibaba.otter.shared.communication.core.impl.connection;

import com.alibaba.otter.shared.communication.core.exception.CommunicationException;
import com.alibaba.otter.shared.communication.core.model.CommunicationParam;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 可被链接池管理的对象, @see {@linkplain CommunicationConnectionPoolableFactory}
 * 
 * @author jianghang 2011-9-9 下午05:01:14
 */
public class CommunicationConnectionPoolable implements CommunicationConnection {

    private CommunicationConnectionPoolFactory pool;
    private CommunicationConnection            delegate;

    public CommunicationConnectionPoolable(CommunicationConnection connection, CommunicationConnectionPoolFactory pool){
        this.delegate = connection;
        this.pool = pool;
    }

    public Object call(Event event) {
        return getDelegate().call(event);
    }

    public void close() throws CommunicationException {
        pool.releaseConnection(this);
    }

    public CommunicationParam getParams() {
        return getDelegate().getParams();
    }

    /**
     * @return 返回原始connection对象
     */
    public CommunicationConnection getDelegate() {
        return this.delegate;
    }

}
