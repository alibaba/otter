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

package com.alibaba.otter.manager.deployer;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 嵌入式jetty启动
 * 
 * @author jianghang 2013-8-8 下午4:04:17
 * @since 4.2.0
 */
public class JettyEmbedServer {

    private static final Logger logger         = LoggerFactory.getLogger(JettyEmbedServer.class);
    private static final String DEFAULT_CONFIG = "jetty.xml";
    private Server              server;
    private String              config         = DEFAULT_CONFIG;

    public JettyEmbedServer(String jettyXml){
        this.config = jettyXml;
    }

    public void start() throws Exception {
        Resource configXml = Resource.newSystemResource(config);
        XmlConfiguration configuration = new XmlConfiguration(configXml.getInputStream());
        server = (Server) configuration.configure();

        //        Integer port = getPort();
        //        if (port != null && port > 0) {
        //            Connector[] connectors = server.getConnectors();
        //            for (Connector connector : connectors) {
        //                connector.setPort(port);
        //            }
        //        }
        Handler handler = server.getHandler();
        if (handler != null && handler instanceof WebAppContext) {
            WebAppContext webAppContext = (WebAppContext) handler;
            webAppContext.setResourceBase(JettyEmbedServer.class.getResource("/webapp").toString());
        }
        server.start();
        if (logger.isInfoEnabled()) {
            logger.info("##Jetty Embed Server is startup!");
        }
    }

    public void join() throws Exception {
        server.join();
        if (logger.isInfoEnabled()) {
            logger.info("##Jetty Embed Server joined!");
        }
    }

    public void stop() throws Exception {
        server.stop();
        if (logger.isInfoEnabled()) {
            logger.info("##Jetty Embed Server is stop!");
        }
    }

    // ================ setter / getter ================

    public void setConfig(String config) {
        this.config = config;
    }

}
