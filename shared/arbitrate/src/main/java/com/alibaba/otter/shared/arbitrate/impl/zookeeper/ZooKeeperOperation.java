package com.alibaba.otter.shared.arbitrate.impl.zookeeper;

import org.apache.zookeeper.KeeperException;

/**
 * {@linkplain ZkClientx}的callback接口
 */
public interface ZooKeeperOperation<T> {

    public T execute() throws KeeperException, InterruptedException;
}
