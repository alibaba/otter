package com.alibaba.otter.shared.communication.core.impl.rmi;

import com.alibaba.otter.shared.communication.core.CommunicationEndpoint;
import com.alibaba.otter.shared.communication.core.exception.CommunicationException;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnection;
import com.alibaba.otter.shared.communication.core.model.CommunicationParam;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 对应rmi的connection实现
 * 
 * @author jianghang 2011-9-9 下午05:26:44
 */
public class RmiCommunicationConnection implements CommunicationConnection {

    private CommunicationEndpoint endpoint;
    private CommunicationParam   params;

    public RmiCommunicationConnection(CommunicationParam params, CommunicationEndpoint endpoint) {
        this.params = params;
        this.endpoint = endpoint;
    }

    public void close() throws CommunicationException {
        // do nothing
    }

    public Object call(Event event) {
        // 调用rmi传递数据到目标server上
        return endpoint.acceptEvent(event);
    }

    @Override
    public CommunicationParam getParams() {
        return params;
    }
}
