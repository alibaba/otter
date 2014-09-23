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

package com.alibaba.otter.shared.common.model.autokeeper;

import java.util.HashSet;
import java.util.Set;

/**
 * 客户端链接统计信息
 * 
 * <pre>
 * 命令: echo cons | nc 127.0.0.1 2181
 *  /127.0.0.1:60731[1](queued=0,recved=1506932,sent=2005617,sid=0x6339e69e49350004,lop=GETC,est=1348196892287,to=10000,lcxid=0x16fe71,lzxid=0x1b03dbc434,lresp=1348209252756,llat=0,minlat=0,avglat=0,maxlat=60)
 * </pre>
 * 
 * @author jianghang 2012-9-21 下午02:13:19
 * @version 4.1.0
 */
public class AutoKeeperConnectionStat extends AutoKeeperStateStat {

    private static final long            serialVersionUID = -786367247388065889L;
    private String                       sessionId;
    private String                       serverAddress;
    private String                       clientAddress;
    private Set<AutoKeeperWatchStat>     watchStats       = new HashSet<AutoKeeperWatchStat>();    // watcher请求状态
    private Set<AutoKeeperEphemeralStat> ephemeralStats   = new HashSet<AutoKeeperEphemeralStat>(); // 临时节点状态

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Set<AutoKeeperWatchStat> getWatchStats() {
        return watchStats;
    }

    public void setWatchStats(Set<AutoKeeperWatchStat> watchStats) {
        this.watchStats = watchStats;
    }

    public Set<AutoKeeperEphemeralStat> getEphemeralStats() {
        return ephemeralStats;
    }

    public void setEphemeralStats(Set<AutoKeeperEphemeralStat> ephemeralStats) {
        this.ephemeralStats = ephemeralStats;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clientAddress == null) ? 0 : clientAddress.hashCode());
        result = prime * result + ((serverAddress == null) ? 0 : serverAddress.hashCode());
        result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AutoKeeperConnectionStat)) {
            return false;
        }
        AutoKeeperConnectionStat other = (AutoKeeperConnectionStat) obj;
        if (clientAddress == null) {
            if (other.clientAddress != null) {
                return false;
            }
        } else if (!clientAddress.equals(other.clientAddress)) {
            return false;
        }
        if (serverAddress == null) {
            if (other.serverAddress != null) {
                return false;
            }
        } else if (!serverAddress.equals(other.serverAddress)) {
            return false;
        }
        if (sessionId == null) {
            if (other.sessionId != null) {
                return false;
            }
        } else if (!sessionId.equals(other.sessionId)) {
            return false;
        }
        return true;
    }

}
