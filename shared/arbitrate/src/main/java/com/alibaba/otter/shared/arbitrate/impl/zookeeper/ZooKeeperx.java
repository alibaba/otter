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

package com.alibaba.otter.shared.arbitrate.impl.zookeeper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;

/**
 * 扩展ZooKeeper，主要是统一处理下链接异常和ACL管理 <br/>
 * <p>
 * document : <br/>
 * <a href="http://wiki.apache.org/hadoop/ZooKeeper/FAQ#A3">http://wiki.apache. org/hadoop/ZooKeeper/FAQ#A3</a> <br/>
 * <a href="http://wiki.apache.org/hadoop/ZooKeeper/ErrorHandling"
 * >http://wiki.apache.org/hadoop/ZooKeeper/ErrorHandling</a>
 * 
 * @author jianghang 2011-9-23 下午01:38:06
 * @version 4.0.0
 */

@Deprecated
public class ZooKeeperx {

    private static final Logger  logger     = LoggerFactory.getLogger(ZooKeeperx.class);
    // private static final Field zooKeeperField = ReflectionUtils.findField(ZooKeeperClient.class, "zookeeper");
    private static final Integer maxRetry   = 3;                                        // 出现exception，最多重试3次

    private long                 retryDelay = 3000L;
    private List<ACL>            acl        = ZooDefs.Ids.OPEN_ACL_UNSAFE;
    private ZooKeeper            zookeeper;
    private AtomicInteger        cversion   = new AtomicInteger(0);
    private AtomicBoolean        running    = new AtomicBoolean(true);

    public ZooKeeperx(ZooKeeper zookeeper){
        this.zookeeper = zookeeper;
    }

    /**
     * add by ljh at 2012-09-13
     * 
     * <pre>
     * 1. 使用zookeeper过程，针对出现ConnectionLoss异常，比如进行create/setData/delete，操作可能已经在zookeeper server上进行应用
     * 2. 针对SelectStageListener进行processId创建时，会以最后一次创建的processId做为调度id. 如果进行retry，之前成功的processId就会被遗漏了
     * </pre>
     * 
     * @see org.apache.zookeeper.ZooKeeper#create(String path, byte[] path, List acl, CreateMode mode)
     */
    public String createNoRetry(final String path, final byte[] data, final CreateMode mode) throws KeeperException,
                                                                                            InterruptedException {
        return zookeeper.create(path, data, acl, mode);
    }

    /**
     * <pre>
     * 1. 使用zookeeper过程，针对出现ConnectionLoss异常，比如进行create/setData/delete，操作可能已经在zookeeper server上进行应用
     * 2. 针对SelectStageListener进行processId创建时，会以最后一次创建的processId做为调度id. 如果进行retry，之前成功的processId就会被遗漏了
     * </pre>
     * 
     * @see org.apache.zookeeper.ZooKeeper#create(String path, byte[] path, List acl, CreateMode mode)
     */
    public String create(final String path, final byte[] data, final CreateMode mode) throws KeeperException,
                                                                                     InterruptedException {
        if (mode.isSequential()) {
            return zookeeper.create(path, data, acl, mode);
        } else {
            return retryOperation(new ZooKeeperOperation<String>() {

                public String execute() throws KeeperException, InterruptedException {
                    return zookeeper.create(path, data, acl, mode);
                }
            });
        }

    }

    /**
     * @see org.apache.zookeeper.ZooKeeper#delete(String path, int version)
     */
    public void delete(final String path, final int version) throws InterruptedException, KeeperException {
        retryOperation(new ZooKeeperOperation() {

            public Object execute() throws KeeperException, InterruptedException {
                zookeeper.delete(path, version);
                return null;
            }
        });
    }

    /**
     * @see org.apache.zookeeper.ZooKeeper#delete(String path, int version , VoidCallback cb , Object ctx)
     */
    public void delete(final String path, final int version, final VoidCallback cb, final Object ctx)
                                                                                                     throws InterruptedException,
                                                                                                     KeeperException {
        retryOperation(new ZooKeeperOperation() {

            public Object execute() throws KeeperException, InterruptedException {
                zookeeper.delete(path, version, cb, ctx);
                return null;
            }
        });
    }

    /**
     * @see org.apache.zookeeper.ZooKeeper#exists(String path, boolean watch)
     */
    public Stat exists(final String path, final boolean watch) throws KeeperException, InterruptedException {
        return retryOperation(new ZooKeeperOperation<Stat>() {

            public Stat execute() throws KeeperException, InterruptedException {
                return zookeeper.exists(path, watch);
            }
        });

    }

    /**
     * @see org.apache.zookeeper.ZooKeeper#exists(String path, Watcher watcher)
     */
    public Stat exists(final String path, final Watcher watcher) throws KeeperException, InterruptedException {
        return retryOperation(new ZooKeeperOperation<Stat>() {

            public Stat execute() throws KeeperException, InterruptedException {
                return zookeeper.exists(path, watcher);
            }
        });
    }

    /**
     * @see org.apache.zookeeper.ZooKeeper#getChildren(String path, boolean watch)
     */
    public List<String> getChildren(final String path, final boolean watch) throws KeeperException,
                                                                           InterruptedException {
        return retryOperation(new ZooKeeperOperation<List<String>>() {

            public List<String> execute() throws KeeperException, InterruptedException {
                return zookeeper.getChildren(path, watch);
            }
        });

    }

    /**
     * @see org.apache.zookeeper.ZooKeeper#getChildren(String path, boolean watch , Stat stat)
     */
    public List<String> getChildren(final String path, final boolean watch, final Stat stat) throws KeeperException,
                                                                                            InterruptedException {
        return retryOperation(new ZooKeeperOperation<List<String>>() {

            public List<String> execute() throws KeeperException, InterruptedException {
                return zookeeper.getChildren(path, watch, stat);
            }
        });

    }

    /**
     * @see org.apache.zookeeper.ZooKeeper#getChildren(String path, Watcher watcher)
     */
    public List<String> getChildren(final String path, final Watcher watcher) throws KeeperException,
                                                                             InterruptedException {
        return retryOperation(new ZooKeeperOperation<List<String>>() {

            public List<String> execute() throws KeeperException, InterruptedException {
                return zookeeper.getChildren(path, watcher);
            }
        });
    }

    /**
     * @see org.apache.zookeeper.ZooKeeper#getData(String path, boolean watch, Stat stat)
     */
    public byte[] getData(final String path, final boolean watch, final Stat stat) throws KeeperException,
                                                                                  InterruptedException {
        return retryOperation(new ZooKeeperOperation<byte[]>() {

            public byte[] execute() throws KeeperException, InterruptedException {
                return zookeeper.getData(path, watch, stat);
            }
        });

    }

    /**
     * @see org.apache.zookeeper.ZooKeeper#getData(String path, Watcher watcher, Stat stat)
     */
    public byte[] getData(final String path, final Watcher watcher, final Stat stat) throws KeeperException,
                                                                                    InterruptedException {
        return retryOperation(new ZooKeeperOperation<byte[]>() {

            public byte[] execute() throws KeeperException, InterruptedException {
                return zookeeper.getData(path, watcher, stat);
            }
        });
    }

    /**
     * @see org.apache.zookeeper.ZooKeeper#setData(String path, byte[] data, int version)
     */
    public Stat setData(final String path, final byte[] data, final int version) throws KeeperException,
                                                                                InterruptedException {
        return retryOperation(new ZooKeeperOperation<Stat>() {

            public Stat execute() throws KeeperException, InterruptedException {
                return zookeeper.setData(path, data, version);
            }
        });
    }

    // =========================== helper method =============================

    /**
     * 包装重试策略
     */
    public <T> T retryOperation(ZooKeeperOperation<T> operation) throws KeeperException, InterruptedException {
        if (!running.get()) {
            throw new ArbitrateException("Zookeeper is destory ,should never be used ....");
        }

        KeeperException exception = null;
        for (int i = 0; i < maxRetry; i++) {
            int version = cversion.get(); // 获取版本
            int retryCount = i + 1;
            try {
                if (!zookeeper.getState().isAlive()) {
                    retryDelay(retryCount);
                    cleanup(version);
                } else {
                    return (T) operation.execute();
                }
            } catch (KeeperException.SessionExpiredException e) {
                logger.warn("Session expired for: " + this + " so reconnecting " + (i + 1) + " times due to: " + e, e);
                retryDelay(retryCount);
                cleanup(version);
            } catch (KeeperException.ConnectionLossException e) { // 特殊处理Connection Loss
                if (exception == null) {
                    exception = e;
                }
                logger.warn("Attempt " + retryCount + " failed with connection loss so " + "attempting to reconnect: "
                            + e, e);
                retryDelay(retryCount);
            }
        }

        throw exception;
    }

    private void retryDelay(int attemptCount) {
        if (attemptCount > 0) {
            try {
                Thread.sleep(attemptCount * retryDelay);
            } catch (InterruptedException e) {
                logger.warn("Failed to sleep: " + e, e);
            }
        }
    }

    public ZooKeeper getDelegate() {
        return this.zookeeper;
    }

    private synchronized void cleanup(int version) { // 加锁操作，阻塞等待上一次的重建完成
        // if (cversion.compareAndSet(version, version + 1)) {// 看一下version是否有变化，没变化就我来更新
        // // ReflectionUtils.makeAccessible(zooKeeperField);
        // // ReflectionUtils.setField(zooKeeperField, new ZooKeeperClient(), null);// 清空
        // ZooKeeper oldZookeepre = zookeeper;
        // zookeeper = ZooKeeperClient.createZookeeper();
        //
        // try {
        // oldZookeepre.close(); // 关闭老链接
        // } catch (InterruptedException e) {
        // // ignore
        // }
        // for (SessionExpiredNotification notification : ZooKeeperClient.getNotifications()) {
        // notification.notification(this);
        // }
        //
        // cversion.incrementAndGet();// 更新一下版本号
        // } else {
        // // 更新失败，忽略。说明已经有别的线程重建过zookeeper，再做一次尝试
        // }
    }

    public void destory() {
        if (running.compareAndSet(true, false)) {
            try {
                zookeeper.close();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    // ===================== setter / getter =================

    public List<ACL> getAcl() {
        return acl;
    }

    public void setAcl(List<ACL> acl) {
        this.acl = acl;
    }

    public long getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }
}
