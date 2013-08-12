package com.alibaba.otter.shared.arbitrate.impl.zookeeper.lock;

import org.springframework.util.Assert;

/**
 * 支持排序的节点
 * 
 * @author jianghang 2011-9-29 下午01:30:36
 * @version 4.0.0
 */
public class LockNode implements Comparable<LockNode> {

    private final String name;
    private String       prefix;
    private int          sequence = -1;

    public LockNode(String name){
        Assert.notNull(name, "id cannot be null");
        this.name = name;
        this.prefix = name;
        int idx = name.lastIndexOf('-');
        if (idx >= 0) {
            this.prefix = name.substring(0, idx);
            try {
                this.sequence = Integer.parseInt(name.substring(idx + 1));
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public int compareTo(LockNode that) {
        int s1 = this.sequence;
        int s2 = that.sequence;
        if (s1 == -1 && s2 == -1) {
            return this.name.compareTo(that.name);
        }

        if (s1 == -1) {
            return -1;
        } else if (s2 == -1) {
            return 1;
        } else {
            return s1 - s2;
        }
    }

    public String getName() {
        return name;
    }

    public int getSequence() {
        return sequence;
    }

    public String getPrefix() {
        return prefix;
    }

    public String toString() {
        return name.toString();
    }

    // ==================== hashcode & equals方法=======================

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        LockNode other = (LockNode) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
