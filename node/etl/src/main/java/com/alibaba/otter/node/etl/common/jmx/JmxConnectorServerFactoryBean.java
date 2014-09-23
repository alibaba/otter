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

package com.alibaba.otter.node.etl.common.jmx;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.MessageFormat;

import javax.management.JMException;

import org.springframework.jmx.support.ConnectorServerFactoryBean;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.shared.common.model.config.node.Node;

/**
 * 扩展实现spring的jmx connector
 * 
 * @author jianghang 2012-7-30 下午12:50:05
 */
public class JmxConnectorServerFactoryBean extends ConnectorServerFactoryBean {

    private String              SERVER_URL           = "service:jmx:rmi://127.0.0.1:{0}/jndi/rmi://127.0.0.1:{0}/mbean";
    private boolean             alwaysCreateRegistry = false;
    private ConfigClientService configClientService;

    public void afterPropertiesSet() throws JMException, IOException {
        Node node = configClientService.currentNode();
        int port = node.getPort().intValue() + 1;
        Integer mbeanPort = node.getParameters().getMbeanPort();
        if (mbeanPort != null && mbeanPort != 0) {// 做个兼容处理，<=4.2.2版本没有mbeanPort设置
            port = mbeanPort;
        }

        String serviceUrl = MessageFormat.format(SERVER_URL, String.valueOf(port));
        super.setServiceUrl(serviceUrl);
        super.setObjectName("connector:name=rmi");
        // 直接使用port
        getRegistry(port);
        super.afterPropertiesSet();
    }

    private Registry getRegistry(int registryPort) throws RemoteException {
        if (this.alwaysCreateRegistry) {
            logger.info("Creating new RMI registry");
            return LocateRegistry.createRegistry(registryPort);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Looking for RMI registry at port '" + registryPort + "'");
        }
        try {
            // Retrieve existing registry.
            Registry reg = LocateRegistry.getRegistry(registryPort);
            testRegistry(reg);
            return reg;
        } catch (RemoteException ex) {
            logger.debug("RMI registry access threw exception", ex);
            logger.info("Could not detect RMI registry - creating new one");
            // Assume no registry found -> create new one.
            return LocateRegistry.createRegistry(registryPort);
        }
    }

    private void testRegistry(Registry registry) throws RemoteException {
        registry.list();
    }

    public void setServiceUrl(String serviceUrl) {
        throw new UnsupportedOperationException("set serviceUrl is not support!");
    }

    public void setAlwaysCreateRegistry(boolean alwaysCreateRegistry) {
        this.alwaysCreateRegistry = alwaysCreateRegistry;
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

}
