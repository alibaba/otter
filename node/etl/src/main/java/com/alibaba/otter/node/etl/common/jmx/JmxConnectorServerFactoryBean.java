package com.alibaba.otter.node.etl.common.jmx;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.MessageFormat;

import javax.management.JMException;

import org.springframework.jmx.support.ConnectorServerFactoryBean;

import com.alibaba.otter.node.common.config.ConfigClientService;

/**
 * 扩展实现spring的jmx connector
 * 
 * @author jianghang 2012-7-30 下午12:50:05
 */
public class JmxConnectorServerFactoryBean extends ConnectorServerFactoryBean {

    private String              SERVER_URL           = "service:jmx:rmi://127.0.0.1/jndi/rmi://127.0.0.1:{0}/mbean";
    private boolean             alwaysCreateRegistry = false;
    private ConfigClientService configClientService;

    public void afterPropertiesSet() throws JMException, IOException {
        int port = configClientService.currentNode().getPort().intValue() + 1; // 默认为通讯端口+1
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
