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

import java.util.List;

/**
 * ephemeral节点的相关统计
 * 
 * <pre>
 * 命令：echo dump | nc 127.0.0.1 2181
 * Sessions with Ephemerals (4):
 * 0x3437a299be280538:
 *     /otter/channel/304/388/mainstem
 *     /otter/node/2
 * </pre>
 * 
 * @author jianghang 2012-9-21 下午02:25:00
 * @version 4.1.0
 */
public class AutoKeeperEphemeralStat extends AutoKeeperStat {

    private static final long serialVersionUID = 7706173088583901348L;
    private String            sessionId;
    private List<String>      paths;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AutoKeeperEphemeralStat)) {
            return false;
        }
        AutoKeeperEphemeralStat other = (AutoKeeperEphemeralStat) obj;
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
