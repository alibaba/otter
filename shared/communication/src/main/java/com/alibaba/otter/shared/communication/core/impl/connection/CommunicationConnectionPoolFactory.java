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

package com.alibaba.otter.shared.communication.core.impl.connection;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;

import com.alibaba.otter.shared.communication.core.exception.CommunicationException;
import com.alibaba.otter.shared.communication.core.impl.rmi.RmiCommunicationConnectionFactory;
import com.alibaba.otter.shared.communication.core.model.CommunicationParam;

/**
 * @author jianghang 2011-9-9 下午05:48:13
 */
public class CommunicationConnectionPoolFactory implements CommunicationConnectionFactory {

    private volatile GenericKeyedObjectPool pool      = null;
    private CommunicationConnectionFactory  factory   = new RmiCommunicationConnectionFactory();
    private int                             maxActive = 10;

    public CommunicationConnectionPoolFactory(){
    }

    public CommunicationConnectionPoolFactory(CommunicationConnectionFactory factory){
        this.factory = factory;
        initial();
    }

    public void initial() {
        if (factory == null) {
            throw new IllegalArgumentException("factory is null!");
        }

        // 创建链接池对象
        pool = new GenericKeyedObjectPool();
        pool.setMaxActive(maxActive);
        pool.setMaxIdle(maxActive);
        pool.setMinIdle(0);
        pool.setMaxWait(60 * 1000); // 60s
        pool.setTestOnBorrow(false);
        pool.setTestOnReturn(false);
        pool.setTimeBetweenEvictionRunsMillis(10 * 1000);
        pool.setNumTestsPerEvictionRun(maxActive * 2);
        pool.setMinEvictableIdleTimeMillis(30 * 60 * 1000);
        pool.setTestWhileIdle(true);
        pool.setFactory(new CommunicationConnectionPoolableFactory(factory)); // 设置连接池管理对象
    }

    public void destory() {
        try {
            pool.close();
        } catch (Exception e) {
            throw new CommunicationException("Connection_Pool_Close_Error", e);
        }
    }

    public CommunicationConnection createConnection(CommunicationParam params) {
        try {
            CommunicationConnectionPoolable poolable = new CommunicationConnectionPoolable(
                                                                                           (CommunicationConnection) pool.borrowObject(params),
                                                                                           this);
            return poolable;
        } catch (Exception e) {
            throw new CommunicationException("createConnection_error", e);
        }
    }

    public void releaseConnection(CommunicationConnection connection) {
        try {
            pool.returnObject(connection.getParams(), connection);
        } catch (Exception e) {
            throw new CommunicationException("releaseConnection_error", e);
        }
    }

    // ======================= setter / getter =======================

    public void setFactory(CommunicationConnectionFactory factory) {
        this.factory = factory;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

}
