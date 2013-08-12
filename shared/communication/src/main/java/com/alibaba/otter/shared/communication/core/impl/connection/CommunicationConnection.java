package com.alibaba.otter.shared.communication.core.impl.connection;

import com.alibaba.otter.shared.communication.core.exception.CommunicationException;
import com.alibaba.otter.shared.communication.core.model.CommunicationParam;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 通讯链接
 * 
 * @author jianghang 2011-9-9 下午04:55:25
 */
public interface CommunicationConnection {

    public Object call(Event event);

    public CommunicationParam getParams();

    public void close() throws CommunicationException;
}
