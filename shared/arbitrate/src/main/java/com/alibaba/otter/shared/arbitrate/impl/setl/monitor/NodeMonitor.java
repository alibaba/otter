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

package com.alibaba.otter.shared.arbitrate.impl.setl.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.listener.NodeListener;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * otter所有node节点监控
 * 
 * @author jianghang 2012-8-29 下午01:00:43
 * @version 4.1.0
 */
public class NodeMonitor implements Monitor {

    private static final Logger logger     = LoggerFactory.getLogger(NodeMonitor.class);

    private ExecutorService     arbitrateExecutor;
    private ZkClientx           zookeeper  = ZooKeeperClient.getInstance();
    private List<NodeListener>  listeners  = Collections.synchronizedList(new ArrayList<NodeListener>());
    private volatile List<Long> aliveNodes = new ArrayList<Long>();                                      // se模块存活的节点
    private IZkChildListener    childListener;

    public NodeMonitor(){
        childListener = new IZkChildListener() {

            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                if (currentChilds != null) {
                    initNodes(currentChilds);
                }
            }
        };
        List<String> childs = zookeeper.subscribeChildChanges(ArbitrateConstants.NODE_NID_ROOT, childListener);
        if (childs == null) {//如果为null，代表系统节点为初始化
            try {
                zookeeper.createPersistent(ArbitrateConstants.NODE_NID_ROOT, true);
            } catch (ZkNodeExistsException e) {
                //ignore 
            }

            childs = zookeeper.getChildren(ArbitrateConstants.NODE_NID_ROOT);
        }

        initNodes(childs);
        // syncNodes();// 开始监视node节点的变化
        MonitorScheduler.register(this);
    }

    public void reload() {
        try {
            initNodes();// 更新数据
        } catch (Exception e) {
        }

    }

    public void destory() {
        listeners.clear();

        zookeeper.unsubscribeChildChanges(ArbitrateConstants.NODE_NID_ROOT, childListener);
        MonitorScheduler.unRegister(this);
    }

    /**
     * 返回当前存活的node列表
     */
    public List<Long> getAliveNodes(boolean reload) {
        if (reload) {
            initNodes();
        }

        return new ArrayList<Long>(aliveNodes);
    }

    /**
     * 返回当前存活的node列表
     */
    public List<Long> getAliveNodes() {
        return getAliveNodes(false);
    }

    private void initNodes() {
        // 获取一下当前存活的所有node节点，再根据对应pipeline关联的node，检查是否在对应的存活列表里
        List<String> nodes = zookeeper.getChildren(ArbitrateConstants.NODE_NID_ROOT);
        initNodes(nodes);
    }

    private synchronized void initNodes(List<String> nodes) {
        List<Long> nids = new ArrayList<Long>();
        for (String node : nodes) {
            if (StringUtils.isNumeric(node)) {
                nids.add(Long.valueOf(node));
            }
        }
        Collections.sort(nids);

        if (!aliveNodes.equals(nids)) {// 不相同，说明有变化
            if (logger.isDebugEnabled()) {
                logger.debug("old aliveNodes{} ,current aliveNodes{}", new Object[] { aliveNodes, nids });
            }

            aliveNodes = nids; // 切换引用，需设置为volatile保证线程安全&可见性
            processChanged(nids);// 通知变化
        }
    }

    // private void syncNodes() {
    // try {
    // List<String> nodes = zookeeper.getChildren(ArbitrateConstants.NODE_NID_ROOT, new AsyncWatcher() {
    //
    // public void asyncProcess(WatchedEvent event) {
    // // 出现session expired/connection losscase下，会触发所有的watcher响应，同时老的watcher会继续保留，所以会导致出现多次watcher响应
    // boolean dataChanged = event.getType() == EventType.NodeDataChanged
    // || event.getType() == EventType.NodeDeleted
    // || event.getType() == EventType.NodeCreated
    // || event.getType() == EventType.NodeChildrenChanged;
    // if (dataChanged) {
    // syncNodes();// 继续关注node节点变化
    // }
    // }
    // });
    //
    // initNodes(nodes);
    // } catch (KeeperException e) {
    // syncNodes();
    // logger.error("", e);
    // } catch (InterruptedException e) {
    // // ignore
    // }
    // }

    // ======================== listener处理 ======================

    public void addListener(NodeListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("## pipeline[{}] add listener [{}]", ClassUtils.getShortClassName(listener.getClass()));
        }

        this.listeners.add(listener);
    }

    public void removeListener(NodeListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("## remove listener [{}]", ClassUtils.getShortClassName(listener.getClass()));
        }

        this.listeners.remove(listener);
    }

    private void processChanged(final List<Long> nodes) {
        for (final NodeListener listener : listeners) {
            // 异步处理
            arbitrateExecutor.submit(new Runnable() {

                public void run() {
                    listener.processChanged(nodes);
                }
            });
        }
    }

    // ========= setter ========

    public void setArbitrateExecutor(ExecutorService arbitrateExecutor) {
        this.arbitrateExecutor = arbitrateExecutor;
    }

}
