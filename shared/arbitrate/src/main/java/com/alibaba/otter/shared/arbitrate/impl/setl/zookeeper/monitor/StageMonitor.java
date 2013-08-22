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

package com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkConnection;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.commons.lang.ClassUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateLifeCycle;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StageComparator;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.Monitor;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.MonitorScheduler;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.AsyncWatcher;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;
import com.alibaba.otter.shared.common.utils.zookeeper.ZooKeeperx;

/**
 * 所有process节点变化的监控
 * 
 * <pre>
 * 监控内容：
 *  1. process列表的删除/新增
 *  2. 每个process下的子节点(setl + end + error)节点的变化信息
 *  
 * 数据获取：
 *  1. 定义{@linkplain StageListener}
 * 接口，并注册。即可监听监控内容的数据变化
 * 
 * 注意点：
 *  stageListener返回的processId为历史数据，可能出现以下情况：
 *  1. 历史已就绪的process，因为系统的error错误或者人为的关闭同步队列，会导致当前的processId被删除
 *  2. 此时该processId仍会被返回，需要在各个event中最后进行检查。判断Permit,是否出现error节点,process是否被删除等
 * 
 * </pre>
 * 
 * @author jianghang 2011-9-21 下午01:16:06
 * @version 4.0.0
 */
public class StageMonitor extends ArbitrateLifeCycle implements Monitor {

    private static final Logger              logger            = LoggerFactory.getLogger(StageMonitor.class);

    private ExecutorService                  arbitrateExecutor;
    private ZkClientx                        zookeeper         = ZooKeeperClient.getInstance();
    private volatile List<Long>              currentProcessIds = new ArrayList<Long>();                                       // 当前的处于监控中的processId列表
    private volatile Map<Long, List<String>> currentStages     = new ConcurrentHashMap<Long, List<String>>();                 // 记录下stages信息

    private List<StageListener>              listeners         = Collections.synchronizedList(new ArrayList<StageListener>());

    private IZkChildListener                 processListener;

    public StageMonitor(Long pipelineId){
        super(pipelineId);

        processListener = new IZkChildListener() {

            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                if (currentChilds != null) {
                    initStage(currentChilds);
                }
            }
        };

        String path = StagePathUtils.getProcessRoot(getPipelineId());
        List<String> childs = zookeeper.subscribeChildChanges(path, processListener);
        initStage(childs);
        // syncStage();
        MonitorScheduler.register(this);
    }

    public void destory() {
        super.destory();
        if (logger.isDebugEnabled()) {
            logger.debug("## destory Stage pipeline[{}]", getPipelineId());
        }

        this.listeners.clear();
        String path = StagePathUtils.getProcessRoot(getPipelineId());
        zookeeper.unsubscribeChildChanges(path, processListener);
        MonitorScheduler.unRegister(this);
    }

    public void reload() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("## reload Stage pipeline[{}]", getPipelineId());
            }

            initStage();
            for (Long processId : currentProcessIds) {
                reload(processId);
            }
        } catch (Exception cause) {
        }
    }

    /**
     * 只reload process这一级别的数据
     */
    public void reloadWithoutStage() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("## reload Stage pipeline[{}]", getPipelineId());
            }

            initStage();
        } catch (Exception cause) {
        }
    }

    /**
     * 重新加载下processId信息
     */
    public void reload(Long processId) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("## reload Stage pipeline[{}] process[{}]", getPipelineId(), processId);
            }

            initProcessStage(processId);
        } catch (Exception cause) {
        }
    }

    /**
     * 获取当前的process id列表
     */
    public List<Long> getCurrentProcessIds() {
        return getCurrentProcessIds(false);
    }

    /**
     * 获取当前的process id列表，指定是否强制刷新
     */
    public List<Long> getCurrentProcessIds(boolean reload) {
        if (reload) {
            reloadWithoutStage();
        }

        return currentProcessIds;
    }

    /**
     * 获取当前的process id对应的stage节点信息
     */
    public List<String> getCurrentStages(Long processId) {
        return getCurrentStages(processId, false);
    }

    /**
     * 获取当前的process id对应的stage节点信息，指定是否强制刷新
     */
    public List<String> getCurrentStages(Long processId, boolean reload) {
        if (reload) {
            reload(processId);
        }

        List<String> stages = currentStages.get(processId);
        if (stages == null) {
            stages = new ArrayList<String>();
        }
        return stages;
    }

    /**
     * 获取zk列表数据，无须同步处理
     */
    private void initStage() {
        // 1. 根据pipelineId构造对应的path
        String path = StagePathUtils.getProcessRoot(getPipelineId());
        // 2. 获取当前的所有process列表
        List<String> currentProcesses = zookeeper.getChildren(path);
        initStage(currentProcesses);
    }

    /**
     * 重新获取最新的process列表
     */
    private void initStage(List<String> currentProcesses) {
        // 3. 循环处理每个process
        List<Long> processIds = new ArrayList<Long>();
        for (String process : currentProcesses) {
            processIds.add(StagePathUtils.getProcessId(process));
        }
        Collections.sort(processIds); // 排序一下
        List<Long> needSyncProcessIds = new ArrayList<Long>();

        synchronized (currentProcessIds) { // 需要同步处理
            for (Long processId : processIds) {
                if (!currentProcessIds.contains(processId)) {
                    needSyncProcessIds.add(processId);
                }
            }

            for (Long currentProcessId : currentProcessIds) {
                if (!processIds.contains(currentProcessId)) {
                    currentStages.remove(currentProcessId);// 如果process已删除,删除本地的stage信息
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("pipeline[{}] old processIds{},current processIds{}", new Object[] { getPipelineId(),
                        currentProcessIds, processIds });
            }

            currentProcessIds = processIds; // 切换引用，需设置为volatile保证线程安全&可见性
        }

        for (Long syncProcessId : needSyncProcessIds) {
            syncStage(syncProcessId);// 开始同步单个的process
            // boolean reply = initProcessStage(processId);// 通知下节点变化
            // if (!currentProcessIds.contains(processId) && reply == false) {
            // syncStage(processId);// 开始同步单个的process
            // } else {
            // // 如果上一次循环中已经添加了监控，则忽略掉本次处理, 避免重复监听
            // }
        }

        if (processIds.size() > 0) {// 尝试触发一下第一个process进行load
            initProcessStage(processIds.get(0));
        }
        processChanged(currentProcessIds);// 通知变化
    }

    private boolean initProcessStage(Long processId) {
        String path = null;
        try {
            // 1. 根据pipelineId+processId构造对应的path
            path = StagePathUtils.getProcess(getPipelineId(), processId);
            // 2. 获取当前的所有process列表
            List<String> currentStages = zookeeper.getChildren(path);
            return initProcessStage(processId, currentStages);
        } catch (ZkNoNodeException e) {
            // ignore，已经被termin处理
            return false;
        } catch (ZkException e) {
            return true;// 走到这一步代表已经出异常了，不需要监听
        }

    }

    /**
     * 处理指定的processId变化，返回结果true代表需要继续监听,false代表不需要监视
     */
    private boolean initProcessStage(Long processId, List<String> currentStages) {
        Collections.sort(currentStages, new StageComparator());
        if (logger.isDebugEnabled()) {
            logger.debug("pipeline[{}] processId[{}] with stage{}", new Object[] { getPipelineId(), processId,
                    currentStages });
        }

        this.currentStages.put(processId, currentStages);// 更新下stage数据
        stageChanged(processId, currentStages);// 通知stage变化
        // 2.1 判断是否存在了loaded节点，此节点为最后一个节点
        if (currentStages.contains(ArbitrateConstants.NODE_TRANSFORMED)) {
            return true;// 不需要监听了
        } else {
            return false;// 其他的状态需要监听
        }
    }

    // /**
    // * 监听process的新增/删除变化，触发process列表的更新
    // */
    // private void syncStage() {
    // // 1. 根据pipelineId构造对应的path
    // String path = null;
    // try {
    // path = StagePathUtils.getProcessRoot(getPipelineId());
    // // 2. 监听当前的process列表的变化
    // List<String> currentProcesses = zookeeper.getChildren(path, new AsyncWatcher() {
    //
    // public void asyncProcess(WatchedEvent event) {
    // MDC.put(ArbitrateConstants.splitPipelineLogFileKey, String.valueOf(getPipelineId()));
    // if (isStop()) {
    // return;
    // }
    //
    // // 出现session expired/connection losscase下，会触发所有的watcher响应，同时老的watcher会继续保留，所以会导致出现多次watcher响应
    // boolean dataChanged = event.getType() == EventType.NodeDataChanged
    // || event.getType() == EventType.NodeDeleted
    // || event.getType() == EventType.NodeCreated
    // || event.getType() == EventType.NodeChildrenChanged;
    // if (dataChanged) {
    // syncStage(); // 继续监听
    // // initStage(); // 重新更新
    // }
    // }
    // });
    //
    // // 3. 循环处理每个process
    // List<Long> processIds = new ArrayList<Long>();
    // for (String process : currentProcesses) {
    // processIds.add(StagePathUtils.getProcessId(process));
    // }
    //
    // Collections.sort(processIds); // 排序一下
    // // 判断一下当前processIds和当前内存中的记录是否有差异，如果有差异立马触发一下
    // if (!currentProcessIds.equals(processIds)) {
    // initStage(); // 立马触发一下
    // }
    // } catch (KeeperException e) {
    // syncStage(); // 继续监听
    // } catch (InterruptedException e) {
    // // ignore
    // }
    // }

    /**
     * 监听指定的processId节点的变化
     */
    private void syncStage(final Long processId) {
        // 1. 根据pipelineId + processId构造对应的path
        String path = null;
        try {
            path = StagePathUtils.getProcess(getPipelineId(), processId);
            // 2. 监听当前的process列表的变化
            IZkConnection connection = zookeeper.getConnection();
            // zkclient包装的是一个持久化的zk，分布式lock只需要一次性的watcher，需要调用原始的zk链接进行操作
            ZooKeeper orginZk = ((ZooKeeperx) connection).getZookeeper();
            List<String> currentStages = orginZk.getChildren(path, new AsyncWatcher() {

                public void asyncProcess(WatchedEvent event) {
                    MDC.put(ArbitrateConstants.splitPipelineLogFileKey, String.valueOf(getPipelineId()));
                    if (isStop()) {
                        return;
                    }

                    if (event.getType() == EventType.NodeDeleted) {
                        processTermined(processId); // 触发下节点删除
                        return;
                    }

                    // 出现session expired/connection losscase下，会触发所有的watcher响应，同时老的watcher会继续保留，所以会导致出现多次watcher响应
                    boolean dataChanged = event.getType() == EventType.NodeDataChanged
                                          || event.getType() == EventType.NodeDeleted
                                          || event.getType() == EventType.NodeCreated
                                          || event.getType() == EventType.NodeChildrenChanged;
                    if (dataChanged) {
                        // boolean reply = initStage(processId);
                        // if (reply == false) {// 出现过load后就不需要再监听变化，剩下的就是节点的删除操作
                        syncStage(processId);
                        // }
                    }
                }
            });

            Collections.sort(currentStages, new StageComparator());
            List<String> lastStages = this.currentStages.get(processId);
            if (lastStages == null || !lastStages.equals(currentStages)) {
                initProcessStage(processId); // 存在差异，立马触发一下
            }

        } catch (NoNodeException e) {
            processTermined(processId); // 触发下节点删除
        } catch (KeeperException e) {
            syncStage(processId);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    // ======================== listener处理 ======================

    public void addListener(StageListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("## pipeline[{}] add listener [{}]", getPipelineId(),
                         ClassUtils.getShortClassName(listener.getClass()));
        }

        this.listeners.add(listener);
    }

    public void removeListener(StageListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("## pipeline[{}] remove listener [{}]", getPipelineId(),
                         ClassUtils.getShortClassName(listener.getClass()));
        }

        this.listeners.remove(listener);
    }

    private void processChanged(final List<Long> processIds) {
        for (final StageListener listener : listeners) {
            // 异步处理
            arbitrateExecutor.submit(new Runnable() {

                public void run() {
                    MDC.put(ArbitrateConstants.splitPipelineLogFileKey, String.valueOf(getPipelineId()));
                    listener.processChanged(processIds);
                }
            });
        }
    }

    private void stageChanged(final Long processId, final List<String> stages) {
        for (final StageListener listener : listeners) {
            // 异步处理
            arbitrateExecutor.submit(new Runnable() {

                public void run() {
                    MDC.put(ArbitrateConstants.splitPipelineLogFileKey, String.valueOf(getPipelineId()));
                    listener.stageChannged(processId, stages);
                }
            });
        }
    }

    private void processTermined(final Long processId) {
        for (final StageListener listener : listeners) {
            // 异步处理
            arbitrateExecutor.submit(new Runnable() {

                public void run() {
                    MDC.put(ArbitrateConstants.splitPipelineLogFileKey, String.valueOf(getPipelineId()));
                    listener.processTermined(processId);
                }
            });
        }
    }

    // ========== setter =========
    public void setArbitrateExecutor(ExecutorService arbitrateExecutor) {
        this.arbitrateExecutor = arbitrateExecutor;
    }

}
