package com.alibaba.otter.shared.common.model.autokeeper;

/**
 * 当前Quorum类型
 * 
 * @author jianghang 2012-9-21 下午02:03:44
 * @version 4.1.0
 */
public enum AutoKeeperQuorumType {

    LEADER, FOLLOWER, OBSERVER, STANDALONE;

    public boolean isLeader() {
        return this.equals(AutoKeeperQuorumType.LEADER);
    }

    public boolean isFollower() {
        return this.equals(AutoKeeperQuorumType.FOLLOWER);
    }

    public boolean isObserver() {
        return this.equals(AutoKeeperQuorumType.OBSERVER);
    }

    public boolean isStandalone() {
        return this.equals(AutoKeeperQuorumType.STANDALONE);
    }
}
