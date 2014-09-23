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

package com.alibaba.otter.shared.arbitrate.impl.zookeeper.lock;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

import org.I0Itec.zkclient.IZkConnection;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.arbitrate.impl.zookeeper.AsyncWatcher;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.common.utils.lock.BooleanMutex;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;
import com.alibaba.otter.shared.common.utils.zookeeper.ZooKeeperx;

/**
 * 实现一个基于zookeeper的分布式锁 <br/>
 * document : <br/>
 * <a href="http://zookeeper.apache.org/doc/trunk/recipes.html">http://zookeeper.apache.org/doc/trunk/recipes.html</a>
 * 
 * <pre>
 * 使用注意：
 *  传统的{@linkplain ReentrantLock}使用有所区别，ReentrantLock主要用于空只单进程多线程之间的调度，所以要求每个线程使用同一个ReentrantLock实例
 *  而{@linkplain DistributedLock}主要是用于控制多进程的调度，所以如果需要被用来控制多线程时，需要使用不同的DistributedLock实例对象。
 *  <strong>因此单个DistributedLock实例在多个线程中进行lock/unlock操作时会有线程安全问题!!</strong>
 *  
 * 使用例子：
 * <code>
 *         DistributedLock lock = new DistributedLock("/lock/");
 *         try {
 *             lock.lock();
 *             // do something
 *         } catch (InterruptedException e1) {
 *             // 可中断
 *         } catch (KeeperException e1) {
 *             // zookeeper异常
 *         } finally {
 *             try {
 *                 lock.unlock();
 *             } catch (KeeperException e) {
 *                 // zookeeper异常
 *             }
 *         }
 * </code>
 * </pre>
 * 
 * @author jianghang 2011-9-29 上午11:16:07
 * @version 4.0.0
 */
public class DistributedLock {

    private static final Logger  logger    = LoggerFactory.getLogger(DistributedLock.class);
    private static final byte[]  data      = { 0x12, 0x34 };
    // private static final Long DEFAULT_TIMEOUT_PERIOD = 60 * 1000L;
    private ZkClientx            zookeeper = ZooKeeperClient.getInstance();
    private final String         root;                                                      // 根节点路径
    private String               id;
    private LockNode             idName;
    private String               ownerId;
    private String               lastChildId;
    private Throwable            other     = null;
    private KeeperException      exception = null;
    private InterruptedException interrupt = null;

    public DistributedLock(String root){
        this.root = root;
        ensureExists(root);
    }

    /**
     * 尝试获取锁操作，阻塞式可被中断
     */
    public void lock() throws InterruptedException, KeeperException {
        // 可能初始化的时候就失败了
        if (exception != null) {
            throw exception;
        }

        if (interrupt != null) {
            throw interrupt;
        }

        if (other != null) {
            throw new NestableRuntimeException(other);
        }

        if (isOwner()) {// 锁重入
            return;
        }

        BooleanMutex mutex = new BooleanMutex();
        acquireLock(mutex);

        mutex.get();
        // 避免zookeeper重启后导致watcher丢失，会出现死锁使用了超时进行重试
        // try {
        // mutex.get(DEFAULT_TIMEOUT_PERIOD, TimeUnit.MILLISECONDS);// 阻塞等待值为true
        // } catch (TimeoutException e) {
        // if (!mutex.state()) {
        // lock();
        // }
        // }

        if (exception != null) {
            unlock();
            throw exception;
        }

        if (interrupt != null) {
            unlock();
            throw interrupt;
        }

        if (other != null) {
            unlock();
            throw new NestableRuntimeException(other);
        }
    }

    /**
     * 尝试获取锁对象, 不会阻塞
     * 
     * @throws InterruptedException
     * @throws KeeperException
     */
    public boolean tryLock() throws KeeperException {
        // 可能初始化的时候就失败了
        if (exception != null) {
            throw exception;
        }

        if (isOwner()) {// 锁重入
            return true;
        }

        acquireLock(null);

        if (exception != null) {
            unlock();
            throw exception;
        }

        if (interrupt != null) {
            unlock();
            Thread.currentThread().interrupt();
        }

        if (other != null) {
            unlock();
            throw new NestableRuntimeException(other);
        }

        return isOwner();
    }

    /**
     * 释放锁对象
     */
    public void unlock() throws KeeperException {
        if (id != null) {
            zookeeper.delete(root + "/" + id);
            id = null;
            idName = null;
        } else {
            // do nothing
        }
    }

    private void ensureExists(final String path) {
        try {
            if (zookeeper.exists(path)) {
                return;
            }

            zookeeper.create(path, data, CreateMode.PERSISTENT);
        } catch (ZkInterruptedException e) {
            Thread.currentThread().interrupt();
            interrupt = (InterruptedException) e.getCause();
        } catch (ZkException e) {
            exception = (KeeperException) e.getCause();
        }
    }

    /**
     * 返回锁对象对应的path
     */
    public String getRoot() {
        return root;
    }

    /**
     * 判断当前是不是锁的owner
     */
    public boolean isOwner() {
        return id != null && ownerId != null && id.equals(ownerId);
    }

    /**
     * 返回当前的节点id
     */
    public String getId() {
        return this.id;
    }

    // ===================== helper method =============================

    /**
     * 执行lock操作，允许传递watch变量控制是否需要阻塞lock操作
     */
    private Boolean acquireLock(final BooleanMutex mutex) {
        try {
            do {
                if (id == null) {// 构建当前lock的唯一标识
                    long sessionId = getSessionId();
                    String prefix = "x-" + sessionId + "-";
                    // 如果第一次，则创建一个节点
                    String path = zookeeper.create(root + "/" + prefix, data, CreateMode.EPHEMERAL_SEQUENTIAL);
                    int index = path.lastIndexOf("/");
                    id = StringUtils.substring(path, index + 1);
                    idName = new LockNode(id);
                }

                if (id != null) {
                    List<String> names = zookeeper.getChildren(root);
                    if (names.isEmpty()) {
                        logger.warn("lock lost with scene:empty list, id[] and node[]", id, idName);
                        unlock();// 异常情况，退出后重新创建一个
                    } else {
                        // 对节点进行排序
                        SortedSet<LockNode> sortedNames = new TreeSet<LockNode>();
                        for (String name : names) {
                            sortedNames.add(new LockNode(name));
                        }

                        if (sortedNames.contains(idName) == false) {
                            logger.warn("lock lost with scene:not contains ,id[] and node[]", id, idName);
                            unlock();// 异常情况，退出后重新创建一个
                            continue;
                        }

                        // 将第一个节点做为ownerId
                        ownerId = sortedNames.first().getName();
                        if (mutex != null && isOwner()) {
                            mutex.set(true);// 直接更新状态，返回
                            return true;
                        } else if (mutex == null) {
                            return isOwner();
                        }

                        SortedSet<LockNode> lessThanMe = sortedNames.headSet(idName);
                        if (!lessThanMe.isEmpty()) {
                            // 关注一下排队在自己之前的最近的一个节点
                            LockNode lastChildName = lessThanMe.last();
                            lastChildId = lastChildName.getName();
                            // 异步watcher处理
                            IZkConnection connection = zookeeper.getConnection();
                            // zkclient包装的是一个持久化的zk，分布式lock只需要一次性的watcher，需要调用原始的zk链接进行操作
                            ZooKeeper orginZk = ((ZooKeeperx) connection).getZookeeper();
                            Stat stat = orginZk.exists(root + "/" + lastChildId, new AsyncWatcher() {

                                public void asyncProcess(WatchedEvent event) {
                                    if (!mutex.state()) { // 避免重复获取lock
                                        acquireLock(mutex);
                                    } else {
                                        logger.warn("locked successful.");
                                    }
                                }

                            });

                            if (stat == null) {
                                acquireLock(mutex);// 如果节点不存在，需要自己重新触发一下，watcher不会被挂上去
                            }
                        } else {
                            if (isOwner()) {
                                mutex.set(true);
                            } else {
                                logger.warn("lock lost with scene:no less ,id[] and node[]", id, idName);
                                unlock();// 可能自己的节点已超时挂了，所以id和ownerId不相同
                            }
                        }
                    }
                }
            } while (id == null);
        } catch (KeeperException e) {
            exception = e;
            if (mutex != null) {
                mutex.set(true);
            }
        } catch (InterruptedException e) {
            interrupt = e;
            if (mutex != null) {
                mutex.set(true);
            }
        } catch (Throwable e) {
            other = e;
            if (mutex != null) {
                mutex.set(true);
            }
        }

        if (isOwner() && mutex != null) {
            mutex.set(true);
        }
        return Boolean.FALSE;
    }

    private long getSessionId() {
        IZkConnection connection = zookeeper.getConnection();
        return ((ZooKeeperx) connection).getZookeeper().getSessionId();
    }
}
