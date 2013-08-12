package com.alibaba.otter.node.etl.common.pipe.impl.rpc;

import com.alibaba.otter.node.etl.common.pipe.PipeKey;
import com.alibaba.otter.shared.etl.model.Identity;

/**
 * 基于rpc调用的pipe key
 * 
 * @author jianghang 2011-10-17 下午01:26:06
 * @version 4.0.0
 */
public class RpcPipeKey extends PipeKey {

    private static final long serialVersionUID = -9092948280957762259L;
    private Long              nid;                                     // 目标机器的唯一标示id
    private Identity          identity;
    private Long              time;

    public RpcPipeKey(){
        this.time = System.currentTimeMillis();// 随机生成一个值
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public Long getNid() {
        return nid;
    }

    public void setNid(Long nid) {
        this.nid = nid;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identity == null) ? 0 : identity.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RpcPipeKey other = (RpcPipeKey) obj;
        if (identity == null) {
            if (other.identity != null) {
                return false;
            }
        } else if (!identity.equals(other.identity)) {
            return false;
        }
        if (time == null) {
            if (other.time != null) {
                return false;
            }
        } else if (!time.equals(other.time)) {
            return false;
        }
        return true;
    }
}
