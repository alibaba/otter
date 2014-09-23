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

import com.alibaba.otter.shared.communication.core.CommunicationEndpoint;
import com.alibaba.otter.shared.communication.core.exception.CommunicationException;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnection;
import com.alibaba.otter.shared.communication.core.model.CommunicationParam;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 对应rmi的connection实现
 * 
 * @author jianghang 2011-9-9 下午05:26:44
 */
public class RmiCommunicationConnection implements CommunicationConnection {

    private CommunicationEndpoint endpoint;
    private CommunicationParam    params;

    public RmiCommunicationConnection(CommunicationParam params, CommunicationEndpoint endpoint){
        this.params = params;
        this.endpoint = endpoint;
    }

    public void close() throws CommunicationException {
        // do nothing
    }

    public Object call(Event event) {
        // 调用rmi传递数据到目标server上
        return endpoint.acceptEvent(event);
    }

    @Override
    public CommunicationParam getParams() {
        return params;
    }
}
