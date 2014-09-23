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

package com.alibaba.otter.node.common.communication;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.shared.communication.core.CommunicationEndpoint;
import com.alibaba.otter.shared.communication.core.impl.dubbo.DubboCommunicationEndpoint;
import com.alibaba.otter.shared.communication.core.impl.rmi.RmiCommunicationEndpoint;

/**
 * 基于Node节点的endpoint包装
 * 
 * @author jianghang 2011-10-18 下午02:28:25
 * @version 4.0.0
 */
public class NodeCommunicationEndpoint implements InitializingBean, DisposableBean {

    private CommunicationEndpoint endpoint;
    private ConfigClientService   configClientService;

    public void afterPropertiesSet() throws Exception {
        // String ip = config.currentNode().getIp();
        Long port = configClientService.currentNode().getPort();
        if (endpoint instanceof RmiCommunicationEndpoint) {
            RmiCommunicationEndpoint rmiEndpoint = (RmiCommunicationEndpoint) endpoint;
            // rmiEndpoint.setHost(ip);
            rmiEndpoint.setPort(port.intValue());
        }

        if (endpoint instanceof DubboCommunicationEndpoint) {
            DubboCommunicationEndpoint dubboEndpoint = (DubboCommunicationEndpoint) endpoint;
            dubboEndpoint.setPort(port.intValue());
        }
        endpoint.initial();
    }

    public void destroy() throws Exception {
        endpoint.destory();
    }

    // ================= setter / getter ==============

    public void setEndpoint(CommunicationEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

}
