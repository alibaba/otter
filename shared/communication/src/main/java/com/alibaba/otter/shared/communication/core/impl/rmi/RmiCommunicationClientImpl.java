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

package com.alibaba.otter.shared.communication.core.impl.rmi;

import com.alibaba.otter.shared.communication.core.CommunicationClient;
import com.alibaba.otter.shared.communication.core.impl.DefaultCommunicationClientImpl;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnectionFactory;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnectionPoolFactory;

/**
 * 基于rmi的通讯实现
 * 
 * @author jianghang 2011-9-13 下午04:10:30
 */
public class RmiCommunicationClientImpl extends DefaultCommunicationClientImpl implements CommunicationClient {

    // 是否使用链接池
    private boolean poolable = true;

    public void initial() {
        CommunicationConnectionFactory factory = null;
        if (poolable) {
            factory = new CommunicationConnectionPoolFactory(new RmiCommunicationConnectionFactory());
            ((CommunicationConnectionPoolFactory) factory).initial();
        } else {
            factory = new RmiCommunicationConnectionFactory();
        }

        super.setFactory(factory);
    }

    // ============================= setter / getter ==========================

    public void setPoolable(boolean poolable) {
        this.poolable = poolable;
    }
}
