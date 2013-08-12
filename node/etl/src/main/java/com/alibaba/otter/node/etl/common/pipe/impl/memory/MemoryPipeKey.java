package com.alibaba.otter.node.etl.common.pipe.impl.memory;

import com.alibaba.otter.node.etl.common.pipe.PipeKey;
import com.alibaba.otter.shared.etl.model.Identity;

/**
 * 基于内存的pipekey实现
 * 
 * @author jianghang 2011-10-13 下午05:32:04
 * @version 4.0.0
 */
public class MemoryPipeKey extends PipeKey {

    private static final long serialVersionUID = -7478539581294846644L;

    private Identity          identity;
    private Long              time;

    public MemoryPipeKey(){
        this.time = System.currentTimeMillis();// 随机生成一个值
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identity == null) ? 0 : identity.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        MemoryPipeKey other = (MemoryPipeKey) obj;
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
