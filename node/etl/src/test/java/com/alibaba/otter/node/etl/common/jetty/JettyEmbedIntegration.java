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
