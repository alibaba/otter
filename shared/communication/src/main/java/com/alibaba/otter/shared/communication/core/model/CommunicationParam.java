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

package com.alibaba.otter.shared.communication.core.model;

/**
 * 连接参数类，<strong>如果参数有变化，需要酌情考虑是否更新相应的hashcode & equals方法</strong>
 * 
 * @author jianghang 2011-9-9 下午07:16:59
 */
public class CommunicationParam {

    private String              ip;                                 // 通讯ip
    private int                 port;                               // 通讯端口
    private CummunicationMethod comMethod = CummunicationMethod.RMI; // 通讯方式

    /**
     * 远程通讯方式
     */
    private static enum CummunicationMethod {
        RMI;
    }

    // ================ setter / getter ====================

    public CummunicationMethod getComMethod() {
        return comMethod;
    }

    public void setComMethod(String comMethod) {
        CummunicationMethod.valueOf(comMethod);
    }

    public void setComMethod(CummunicationMethod comMethod) {
        this.comMethod = comMethod;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    // ==================== hashcode & equals ===================

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comMethod == null) ? 0 : comMethod.hashCode());
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + port;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CommunicationParam)) {
            return false;
        }
        CommunicationParam other = (CommunicationParam) obj;
        if (comMethod == null) {
            if (other.comMethod != null) {
                return false;
            }
        } else if (!comMethod.equals(other.comMethod)) {
            return false;
        }
        if (ip == null) {
            if (other.ip != null) {
                return false;
            }
        } else if (!ip.equals(other.ip)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return true;
    }

}
