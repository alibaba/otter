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
 * zk服务对应统计信息
 * 
 * <pre>
 * 命令：echo stat | nc 127.0.0.1 2181
 * Zookeeper version: 3.3.6-1366786, built on 07/29/2012 06:22 GMT
 * Clients:
 *  /127.0.0.1:34480[0](queued=0,recved=1,sent=0)
 *  /127.0.0.1:60731[1](queued=0,recved=1853744,sent=2466780)
 * 
 * Latency min/avg/max: 0/0/99
 * Received: 1857451
 * Sent: 2470863
 * Outstanding: 0
 * Zxid: 0x1b03de020b
 * Mode: follower
 * Node count: 1758
 * </pre>
 * 
 * @author jianghang 2012-9-21 下午02:13:40
 * @version 4.1.0
 */
public class AutoKeeperServerStat extends AutoKeeperStateStat {

    private static final long             serialVersionUID = 617926406886982808L;
    private String                        address;
    private String                        version;
    private AutoKeeperQuorumType          quorumType;                                                // 运行类型，leader/follower/observer
    private long                          nodeCount;                                                 // 总的节点数
    private Set<AutoKeeperConnectionStat> connectionStats  = new HashSet<AutoKeeperConnectionStat>(); // 客户端链接状态

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Set<AutoKeeperConnectionStat> getConnectionStats() {
        return connectionStats;
    }

    public void setConnectionStats(Set<AutoKeeperConnectionStat> connectionStats) {
        this.connectionStats = connectionStats;
    }

    public AutoKeeperQuorumType getQuorumType() {
        return quorumType;
    }

    public void setQuorumType(AutoKeeperQuorumType quorumType) {
        this.quorumType = quorumType;
    }

    public long getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(long nodeCount) {
        this.nodeCount = nodeCount;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AutoKeeperServerStat)) {
            return false;
        }
        AutoKeeperServerStat other = (AutoKeeperServerStat) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        return true;
    }

}
