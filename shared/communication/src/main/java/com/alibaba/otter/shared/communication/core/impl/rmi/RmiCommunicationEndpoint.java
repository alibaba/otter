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

package com.alibaba.otter.shared.communication.core.impl.rmi;

import java.rmi.RemoteException;

import org.springframework.remoting.rmi.RmiServiceExporter;

import com.alibaba.otter.shared.communication.core.CommunicationEndpoint;
import com.alibaba.otter.shared.communication.core.exception.CommunicationException;
import com.alibaba.otter.shared.communication.core.impl.AbstractCommunicationEndpoint;

/**
 * 基于rmi的endpoint的实现，包装了一个rmi remote对象
 * 
 * @author jianghang 2011-9-9 下午07:06:25
 */
public class RmiCommunicationEndpoint extends AbstractCommunicationEndpoint {

    private String             host;
    private int                port                 = 1099;
    private RmiServiceExporter export;
    private boolean            alwaysCreateRegistry = false;

    public RmiCommunicationEndpoint(){
    }

    public RmiCommunicationEndpoint(int port){
        this.port = port;
        initial();
    }

    public void initial() {
        export = new RmiServiceExporter();
        export.setServiceName("endpoint");
        export.setService(this);// 暴露自己
        export.setServiceInterface(CommunicationEndpoint.class);
        export.setRegistryHost(host);
        export.setRegistryPort(port);
        export.setAlwaysCreateRegistry(alwaysCreateRegistry);// 强制创建一个

        try {
            export.afterPropertiesSet();
        } catch (RemoteException e) {
            throw new CommunicationException("Rmi_Create_Error", e);
        }

    }

    public void destory() {
        try {
            export.destroy();
        } catch (RemoteException e) {
            throw new CommunicationException("Rmi_Destory_Error", e);
        }
    }

    // =============== setter / gettter ==================
    public void setAlwaysCreateRegistry(boolean alwaysCreateRegistry) {
        this.alwaysCreateRegistry = alwaysCreateRegistry;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

}
