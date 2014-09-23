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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.lang.ClassUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.Assert;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateLifeCycle;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.listener.MainstemListener;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.lock.BooleanMutex;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;
import com.google.common.collect.Lists;

/**
 * 主备切换控制器，active的只有一位，所有的standy都有平等的选择权
 * 
 * <pre>
 * 1. active一旦产生，出现瞬断，在规定的时间内，其享有优先权
 * 2. active一旦产生，如果主动释放其active权利，其他的standby的节点就有机会立即参与选举
 * </pre>
 * 
 * @author jianghang 2012-10-1 下午02:19:22
 * @version 4.1.0
 */
public class MainstemMonitor extends ArbitrateLifeCycle implements Monitor {

    private static final Logger        logger       = LoggerFactory.getLogger(MainstemMonitor.class);
    private ZkClientx                  zookeeper    = ZooKeeperClient.getInstance();
    private ScheduledExecutorService   delayExector = Executors.newScheduledThreadPool(1);
    private int                        delayTime    = 5;
    private volatile MainStemEventData activeData;
    private IZkDataListener            dataListener;
    private BooleanMutex               mutex        = new BooleanMutex(false);
    private volatile boolean           release      = false;
    private List<MainstemListener>     listeners    = Collections.synchronizedList(new ArrayList<MainstemListener>());

    public MainstemMonitor(Long pipelineId){
        super(pipelineId);
        // initMainstem();
        dataListener = new IZkDataListener() {

            public void handleDataChange(String dataPath, Object data) throws Exception {
                MDC.put(ArbitrateConstants.splitPipelineLogFileKey, String.valueOf(getPipelineId()));
                MainStemEventData mainStemData = JsonUtils.unmarshalFromByte((byte[]) data, MainStemEventData.class);
                if (!isMine(mainStemData.getNid())) {
                    mutex.set(false);
                }

                if (!mainStemData.isActive() && isMine(mainStemData.getNid())) { // 说明出现了主动释放的操作，并且本机之前是active
                    release = true;
                    releaseMainstem();// 彻底释放mainstem
                }

                activeData = (MainStemEventData) mainStemData;
            }

            public void handleDataDeleted(String dataPath) throws Exception {
                MDC.put(ArbitrateConstants.splitPipelineLogFileKey, String.valueOf(getPipelineId()));
                mutex.set(false);
                if (!release && isMine(activeData.getNid())) {
                    // 如果上一次active的状态就是本机，则即时触发一下active抢占
                    initMainstem();
                    // } else if (!isMine(activeData.getNid()) && !activeData.isActive()) {
                    // // 针对其他的节点，如果发现上一次的mainstem状态为非active状态，说明存在手工干预进行mainstem切换，可立马进行抢占mainstem
                    // initMainstem();
                } else {
                    // 否则就是等待delayTime，避免因网络瞬端或者zk异常，导致出现频繁的切换操作
                    delayExector.schedule(new Runnable() {

                        public void run() {
                            initMainstem();
                        }
                    }, delayTime, TimeUnit.SECONDS);
                }
            }

        };

        String path = StagePathUtils.getMainStem(getPipelineId());
        zookeeper.subscribeDataChanges(path, dataListener);
        MonitorScheduler.register(this, 5 * 60 * 1000L, 5 * 60 * 1000L); // 5分钟处理一次
    }

    public void reload() {
        if (logger.isDebugEnabled()) {
            logger.debug("## reload mainstem pipeline[{}]", getPipelineId());
        }

        try {
            initMainstem();
        } catch (Exception e) {// 处理下异常
        }
    }

    public void initMainstem() {
        if (isStop()) {
            return;
        }

        PermitMonitor permitMonitor = ArbitrateFactory.getInstance(getPipelineId(), PermitMonitor.class);
        ChannelStatus status = permitMonitor.getChannelPermit(true);
        if (status.isStop()) {
            return; // 如果已经关闭则退出
        }

        Long nid = ArbitrateConfigUtils.getCurrentNid();
        String path = StagePathUtils.getMainStem(getPipelineId());

        MainStemEventData data = new MainStemEventData();
        data.setStatus(MainStemEventData.Status.TAKEING);
        data.setPipelineId(getPipelineId());
        data.setNid(nid);// 设置当前的nid
        // 序列化
        byte[] bytes = JsonUtils.marshalToByte(data);
        try {
            mutex.set(false);
            zookeeper.create(path, bytes, CreateMode.EPHEMERAL);
            activeData = data;
            processActiveEnter();// 触发一下事件
            mutex.set(true);
        } catch (ZkNodeExistsException e) {
            bytes = zookeeper.readData(path, true);
            if (bytes == null) {// 如果不存在节点，立即尝试一次
                initMainstem();
            } else {
                activeData = JsonUtils.unmarshalFromByte(bytes, MainStemEventData.class);
                if (nid.equals(activeData.getNid())) { // reload时会重复创建，如果是自己就触发一下
                    mutex.set(true);
                }
            }
        }
    }

    public void destory() {
        super.destory();

        String path = StagePathUtils.getMainStem(getPipelineId());
        zookeeper.unsubscribeDataChanges(path, dataListener);

        delayExector.shutdownNow(); // 关闭调度
        releaseMainstem();
        MonitorScheduler.unRegister(this);
    }

    public boolean releaseMainstem() {
        if (check()) {
            String path = StagePathUtils.getMainStem(getPipelineId());
            zookeeper.delete(path);
            mutex.set(false);
            processActiveExit();
            return true;
        }

        return false;
    }

    public MainStemEventData getCurrentActiveData() {
        return activeData;
    }

    /**
     * 阻塞等待自己成为active，如果自己成为active，立马返回
     * 
     * @throws InterruptedException
     */
    public void waitForActive() throws InterruptedException {
        initMainstem();
        mutex.get();
    }

    /**
     * 检查当前的状态
     */
    public boolean check() {
        String path = StagePathUtils.getMainStem(getPipelineId());
        try {
            byte[] bytes = zookeeper.readData(path);
            Long nid = ArbitrateConfigUtils.getCurrentNid();
            MainStemEventData eventData = JsonUtils.unmarshalFromByte(bytes, MainStemEventData.class);
            activeData = eventData;// 更新下为最新值
            // 检查下nid是否为自己
            boolean result = nid.equals(eventData.getNid());
            if (!result) {
                logger.warn("mainstem is running in node[{}] , but not in node[{}]", eventData.getNid(), nid);
            }
            return result;
        } catch (ZkNoNodeException e) {
            logger.warn("mainstem is not run any in node");
            return false;
        } catch (ZkInterruptedException e) {
            logger.warn("mainstem check is interrupt");
            Thread.interrupted();// 清除interrupt标记
            return check();
        } catch (ZkException e) {
            logger.warn("mainstem check is failed");
            return false;
        }
    }

    /**
     * 更新mainStem的同步状态数据
     */
    public void single(MainStemEventData data) {
        Assert.notNull(data);
        Long nid = ArbitrateConfigUtils.getCurrentNid();
        if (!check()) {
            return;
        }

        data.setNid(nid);// 设置当前的nid
        String path = StagePathUtils.getMainStem(data.getPipelineId());
        byte[] bytes = JsonUtils.marshalToByte(data);// 初始化的数据对象
        try {
            zookeeper.writeData(path, bytes);
        } catch (ZkException e) {
            throw new ArbitrateException("mainStem_single", data.toString(), e);
        }
        activeData = data;
    }

    // ====================== helper method ======================

    private boolean isMine(Long targetNid) {
        return targetNid.equals(ArbitrateConfigUtils.getCurrentNid());
    }

    public void addListener(MainstemListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("## pipeline[{}] add listener [{}]", ClassUtils.getShortClassName(listener.getClass()));
        }

        this.listeners.add(listener);
    }

    public void removeListener(MainstemListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("## remove listener [{}]", ClassUtils.getShortClassName(listener.getClass()));
        }

        this.listeners.remove(listener);
    }

    private void processActiveEnter() {
        for (final MainstemListener listener : Lists.newArrayList(listeners)) {
            try {
                listener.processActiveEnter();
            } catch (Exception e) {
                logger.error("processSwitchActive failed", e);
            }
        }
    }

    private void processActiveExit() {
        // 注意：在处理响应事件时，会调用remove的操作，可能会导致出现java.util.ConcurrentModificationException异常，所以这里拷贝了一份listener
        for (final MainstemListener listener : Lists.newArrayList(listeners)) {
            try {
                listener.processActiveExit();
            } catch (Exception e) {
                logger.error("processSwitchActive failed", e);
            }
        }
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

}
