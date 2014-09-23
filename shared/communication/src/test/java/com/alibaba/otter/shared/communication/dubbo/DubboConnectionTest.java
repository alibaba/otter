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

package com.alibaba.otter.shared.communication.dubbo;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnection;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnectionFactory;
import com.alibaba.otter.shared.communication.core.impl.dubbo.DubboCommunicationConnectionFactory;
import com.alibaba.otter.shared.communication.core.impl.dubbo.DubboCommunicationEndpoint;
import com.alibaba.otter.shared.communication.core.model.CommunicationParam;
import com.alibaba.otter.shared.communication.core.model.heart.HeartEvent;

public class DubboConnectionTest extends org.jtester.testng.JTester {

    @BeforeClass
    public void initial() {
        // 创建endpoint
        DubboCommunicationEndpoint endpoint = new DubboCommunicationEndpoint(2088);
        endpoint.initial();
    }

    @Test
    public void testSingle() {
        CommunicationConnectionFactory factory = new DubboCommunicationConnectionFactory();
        CommunicationParam param = new CommunicationParam();
        param.setIp("127.0.0.1");
        param.setPort(2088);
        CommunicationConnection connection = factory.createConnection(param);
        Object result = connection.call(new HeartEvent());
        want.object(result).notNull();
    }

}
