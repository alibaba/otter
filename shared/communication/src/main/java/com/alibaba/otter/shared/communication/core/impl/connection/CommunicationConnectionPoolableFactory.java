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

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.alibaba.otter.shared.communication.core.model.CommunicationParam;
import com.alibaba.otter.shared.communication.core.model.heart.HeartEvent;

/**
 * 链接池内容管理工厂, @see {@linkplain GenericObjectPool} , {@linkplain PoolableObjectFactory}
 * 
 * @author jianghang 2011-9-9 下午05:00:15
 */
public class CommunicationConnectionPoolableFactory implements KeyedPoolableObjectFactory {

    private CommunicationConnectionFactory factory;

    public CommunicationConnectionPoolableFactory(CommunicationConnectionFactory factory){
        this.factory = factory;
    }

    public void destroyObject(Object key, Object obj) throws Exception {
        if (obj instanceof CommunicationConnectionPoolable) {
            factory.releaseConnection(((CommunicationConnectionPoolable) obj).getDelegate());// 关闭原始链接
        } else {
            throw new IllegalArgumentException("pool object is not CommunicationConnectionPoolable!");
        }
    }

    public Object makeObject(Object key) throws Exception {
        if (key instanceof CommunicationParam) {
            return factory.createConnection((CommunicationParam) key);// 创建链接
        } else {
            throw new IllegalArgumentException("key object is not CommunicationParams!");
        }
    }

    public void passivateObject(Object key, Object obj) throws Exception {
    }

    public void activateObject(Object key, Object obj) throws Exception {
    }

    public boolean validateObject(Object key, Object obj) {
        if (obj instanceof CommunicationConnectionPoolable) {
            CommunicationConnectionPoolable connection = (CommunicationConnectionPoolable) obj;
            try {
                Object value = connection.call(new HeartEvent());//发起一次心跳检查
                return value != null;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

}
