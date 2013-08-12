package com.alibaba.otter.shared.communication.core.impl.connection;

import com.alibaba.otter.shared.communication.core.exception.CommunicationException;
import com.alibaba.otter.shared.communication.core.model.CommunicationParam;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 可被链接池管理的对象, @see {@linkplain CommunicationConnectionPoolableFactory}
 * 
 * @author jianghang 2011-9-9 下午05:01:14
 */
public class CommunicationConnectionPoolable implements CommunicationConnection {

    private CommunicationConnectionPoolFactory pool;
    private CommunicationConnection     delegate;

    public CommunicationConnectionPoolable(CommunicationConnection connection, CommunicationConnectionPoolFactory pool){
        this.delegate = connection;
        this.pool = pool;
    }

    public Object call(Event event) {
        return getDelegate().call(event);
    }

    public void close() throws CommunicationException {
        pool.releaseConnection(this);
    }

    public CommunicationParam getParams() {
        return getDelegate().getParams();
    }

    /**
     * @return 返回原始connection对象
     */
    public CommunicationConnection getDelegate() {
        return this.delegate;
    }

}
