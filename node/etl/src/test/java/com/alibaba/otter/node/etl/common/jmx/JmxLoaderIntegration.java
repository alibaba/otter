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

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Map;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.jtester.annotations.SpringBeanByName;
import org.springframework.jmx.export.MBeanExporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.BaseOtterTest;

public class JmxLoaderIntegration extends BaseOtterTest {

    static {
        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @SpringBeanByName
    private MBeanExporter exporter;

    @BeforeClass
    public void initial() {
        System.setProperty("nid", "50");
    }

    @Test
    public void test_simple() {
        MBeanServer mBeanServer = exporter.getServer();

        try {
            ObjectName objectName = new ObjectName("bean:name=otterControllor");
            MBeanInfo nodeInfo = mBeanServer.getMBeanInfo(objectName);
            System.out.println(nodeInfo);
            Object result = mBeanServer.getAttribute(objectName, "HeapMemoryUsage");
            System.out.println(result);

            JMXServiceURL address = new JMXServiceURL("service:jmx:rmi://127.0.0.1/jndi/rmi://127.0.0.1:1099/mbean");
            Map environment = null;

            JMXConnector cntor = JMXConnectorFactory.connect(address, environment);
            MBeanServerConnection mbsc = cntor.getMBeanServerConnection();
            String domain = mbsc.getDefaultDomain();
            System.out.println(domain);

            result = mbsc.getAttribute(objectName, "HeapMemoryUsage");
            System.out.println(result);
        } catch (Exception e) {
            want.fail(e.getMessage());
        }
    }
}
