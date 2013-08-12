package com.alibaba.otter.shared.communication.core.impl.rmi;

import com.alibaba.otter.shared.communication.core.CommunicationClient;
import com.alibaba.otter.shared.communication.core.impl.DefaultCommunicationClientImpl;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnectionFactory;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnectionPoolFactory;

/**
 * 基于rmi的通讯实现
 * 
 * @author jianghang 2011-9-13 下午04:10:30
 */
public class RmiCommunicationClientImpl extends DefaultCommunicationClientImpl implements CommunicationClient {

    // 是否使用链接池
    private boolean poolable = true;

    public void initial() {
        CommunicationConnectionFactory factory = null;
        if (poolable) {
            factory = new CommunicationConnectionPoolFactory(new RmiCommunicationConnectionFactory());
            ((CommunicationConnectionPoolFactory) factory).initial();
        } else {
            factory = new RmiCommunicationConnectionFactory();
        }

        super.setFactory(factory);
    }

    // ============================= setter / getter ==========================

    public void setPoolable(boolean poolable) {
        this.poolable = poolable;
    }
}
