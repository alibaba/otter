package com.alibaba.otter.shared.arbitrate.impl.zookeeper.lock;

import java.text.MessageFormat;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.KeeperException;

/**
 * 基于{@linkplain ReentrantLock}和{@linkplain DistributedLock}
 * 的功能组合，实现多进程+多线程全方位的lock控制
 * 
 * @author jianghang 2011-9-30 上午09:48:05
 * @version 4.0.0
 */
public class DistributedReentrantLock extends DistributedLock {

    private static final String ID_FORMAT     = "Thread[{0}] Distributed[{1}]";
    private ReentrantLock       reentrantLock = new ReentrantLock();

    public DistributedReentrantLock(String root) {
        super(root);
    }

    public void lock() throws InterruptedException, KeeperException {
        reentrantLock.lock();//多线程竞争时，先拿到第一层锁
        super.lock();
    }

    public boolean tryLock() throws KeeperException {
        //多线程竞争时，先拿到第一层锁
        return reentrantLock.tryLock() && super.tryLock();
    }

    public void unlock() throws KeeperException {
        super.unlock();
        reentrantLock.unlock();//多线程竞争时，释放最外层锁
    }

    @Override
    public String getId() {
        return MessageFormat.format(ID_FORMAT, Thread.currentThread().getId(), super.getId());
    }

    @Override
    public boolean isOwner() {
        return reentrantLock.isHeldByCurrentThread() && super.isOwner();
    }

}
