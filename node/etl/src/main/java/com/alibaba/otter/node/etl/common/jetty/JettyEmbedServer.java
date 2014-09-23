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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.node.common.config.ConfigClientService;

/**
 * jetty的嵌入式启动入口
 * 
 * @author jianghang 2011-10-18 下午01:58:33
 * @version 4.0.0
 */
public class JettyEmbedServer implements InitializingBean, DisposableBean {

    private static final String DEFAULT_CONFIG = "jetty/jetty.xml";
    private static final Logger logger         = LoggerFactory.getLogger(JettyEmbedServer.class);
    private Server              server;
    private String              config         = DEFAULT_CONFIG;
    private String              htdocsDir;
    private ConfigClientService configClientService;

    public void afterPropertiesSet() throws Exception {
        Resource configXml = Resource.newSystemResource(config);
        XmlConfiguration configuration = new XmlConfiguration(configXml.getInputStream());
        server = (Server) configuration.configure();
        Integer port = getPort();
        if (port != null && port > 0) {
            Connector[] connectors = server.getConnectors();
            for (Connector connector : connectors) {
                connector.setPort(port);
            }
        }

        Handler handler = server.getHandler();
        if (handler != null && handler instanceof ServletContextHandler) {
            ServletContextHandler servletHandler = (ServletContextHandler) handler;
            servletHandler.getInitParams().put("org.eclipse.jetty.servlet.Default.resourceBase", htdocsDir);
        }

        server.start();
        if (logger.isInfoEnabled()) {
            logger.info("##Jetty Embed Server is startup!");
        }
    }

    private Integer getPort() {
        return configClientService.currentNode().getParameters().getDownloadPort();
    }

    public void destroy() throws Exception {
        server.stop();
        if (logger.isInfoEnabled()) {
            logger.info("##Jetty Embed Server is stop!");
        }
    }

    // ================ setter / getter ================

    public void setConfig(String config) {
        this.config = config;
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setHtdocsDir(String htdocsDir) {
        this.htdocsDir = htdocsDir;
    }

}
