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

package com.alibaba.otter.shared.communication;

import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.communication.core.CommunicationClient;
import com.alibaba.otter.shared.communication.core.model.heart.HeartEvent;

/**
 * @author jianghang
 */
public class CommunicationSpringIntegrateTest extends BaseOtterTest {

    @SpringBeanByName
    private CommunicationClient dubboCommunicationClient;

    @SpringBeanByName
    private CommunicationClient rmiPoolCommunicationClient;

    @SpringBeanByName
    private CommunicationClient rmiCommunicationClient;

    @Test
    public void testRmiSingle() {
        Object result = rmiCommunicationClient.call("127.0.0.1:1099", new HeartEvent());
        want.object(result).notNull();
    }

    @Test
    public void testRmiPool() {
        Object result = rmiPoolCommunicationClient.call("127.0.0.1:1099", new HeartEvent());
        want.object(result).notNull();
    }

    @Test
    public void testDubboSingle() {
        Object result = dubboCommunicationClient.call("127.0.0.1:2088", new HeartEvent());
        want.object(result).notNull();
    }

}
