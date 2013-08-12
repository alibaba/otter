package com.alibaba.otter.shared.etl.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * @author xiaoqing.zhouxq 2011-8-15 上午09:10:42
 */
public abstract class BatchObject<T> implements Serializable {

    private static final long serialVersionUID = 3211077130963551303L;
    private Identity          identity;

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public abstract void merge(T data);

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identity == null) ? 0 : identity.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BatchObject other = (BatchObject) obj;
        if (identity == null) {
            if (other.identity != null) return false;
        } else if (!identity.equals(other.identity)) return false;
        return true;
    }

}
