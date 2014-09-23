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

package com.alibaba.otter.node.etl.common.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;

public class JettyEmbedIntegration {

    public static void main(String args[]) throws Exception {
        Resource jetty_xml = Resource.newSystemResource("jetty/jetty.xml");
        XmlConfiguration configuration = new XmlConfiguration(jetty_xml.getInputStream());
        Server server = (Server) configuration.configure();
        int port = 8081;
        Connector[] connectors = server.getConnectors();
        for (Connector connector : connectors) {
            connector.setPort(port);
        }

        Handler handler = server.getHandler();
        if (handler != null && handler instanceof ServletContextHandler) {
            ServletContextHandler servletHandler = (ServletContextHandler) handler;
            servletHandler.getInitParams().put("org.eclipse.jetty.servlet.Default.resourceBase", "/tmp/");
        }

        server.start();
        server.join();
    }
}
