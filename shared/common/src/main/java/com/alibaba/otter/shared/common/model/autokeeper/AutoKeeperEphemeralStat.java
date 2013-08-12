package com.alibaba.otter.shared.common.model.autokeeper;

import java.util.List;

/**
 * ephemeral节点的相关统计
 * 
 * <pre>
 * 命令：echo dump | nc 10.20.144.51 2181
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

    private String       sessionId;
    private List<String> paths;

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
