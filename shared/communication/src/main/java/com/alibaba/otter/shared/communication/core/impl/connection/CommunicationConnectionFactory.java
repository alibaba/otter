package com.alibaba.otter.shared.communication.core.impl.connection;

import com.alibaba.otter.shared.communication.core.model.CommunicationParam;

/**
 * {@linkplain CommunicationConnection}链接创建和关闭工厂
 * 
 * @author jianghang 2011-9-9 下午05:24:09
 */
public interface CommunicationConnectionFactory {

    CommunicationConnection createConnection(CommunicationParam params);

    void releaseConnection(CommunicationConnection connection);
}
