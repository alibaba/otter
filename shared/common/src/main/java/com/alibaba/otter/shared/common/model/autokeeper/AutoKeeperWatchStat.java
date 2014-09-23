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
 * watch对应的状态 <br/>
 * 
 * <pre>
 * 命令1: echo wchc | nc 127.0.0.1 2181
 * ----
 * 0x6339e69e49350004
 *     /otter/channel/304/388/mainstem
 *     /otter/channel/304
 * ----
 * 命令2: echo wchp | nc 127.0.0.1 2181
 * /otter/channel/304/388/mainstem
 *     0x6339e69e49350004
 * /otter/channel/304
 *     0x6339e69e49350004
 * ----
 * 命令3: echo wchs | nc 127.0.0.1 2181
 * 1 connections watching 2 paths
 * Total watches:2
 * </pre>
 * 
 * @author jianghang 2012-9-21 下午02:19:30
 * @version 4.1.0
 */
public class AutoKeeperWatchStat extends AutoKeeperStat {

    private static final long serialVersionUID = -448913735928277986L;
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
        if (!(obj instanceof AutoKeeperWatchStat)) {
            return false;
        }
        AutoKeeperWatchStat other = (AutoKeeperWatchStat) obj;
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
