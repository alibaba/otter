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

import java.text.MessageFormat;

import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import com.alibaba.otter.shared.communication.core.CommunicationEndpoint;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnection;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnectionFactory;
import com.alibaba.otter.shared.communication.core.model.CommunicationParam;

/**
 * 基于rmi的通讯链接实现
 * 
 * @author jianghang 2011-9-9 下午04:58:28
 */
public class RmiCommunicationConnectionFactory implements CommunicationConnectionFactory {

    static {
        // 初始化rmi相关的参数
        System.setProperty("sun.rmi.transport.connectTimeout", "30000"); // 连接超时
    }

    private final String RMI_SERVICE_URL = "rmi://{0}:{1}/endpoint";

    @Override
    public CommunicationConnection createConnection(CommunicationParam params) {
        if (params == null) {
            throw new IllegalArgumentException("param is null!");
        }

        // 构造对应的url
        String serviceUrl = MessageFormat.format(RMI_SERVICE_URL, params.getIp(), String.valueOf(params.getPort()));
        // 自己实现的有连接池的Stub
        RmiProxyFactoryBean proxy = new RmiProxyFactoryBean();
        proxy.setServiceUrl(serviceUrl);
        proxy.setServiceInterface(CommunicationEndpoint.class);
        proxy.afterPropertiesSet();
        return new RmiCommunicationConnection(params, (CommunicationEndpoint) proxy.getObject());// 创建链接
    }

    @Override
    public void releaseConnection(CommunicationConnection connection) {
        // do nothing
    }

}
