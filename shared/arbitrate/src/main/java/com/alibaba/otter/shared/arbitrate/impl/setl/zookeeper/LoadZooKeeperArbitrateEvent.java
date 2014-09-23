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

package com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper;

import java.util.Date;

import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.LoadArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.LoadStageListener;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 关注transformed节点，创建loaded节点
 * 
 * @version 4.0.3
 * 
 * <pre>
 * 1. 去除对应的DistributedLock的操作，减少中美同步时的latency. 每次DistributedLock操作都会涉及中美zk集群的交互，延迟在300ms
 * 
 * </pre>
 * @author jianghang 2011-8-9 下午05:10:50
 */
public class LoadZooKeeperArbitrateEvent implements LoadArbitrateEvent {

    private static final Logger           logger    = LoggerFactory.getLogger(LoadZooKeeperArbitrateEvent.class);
    private ZkClientx                     zookeeper = ZooKeeperClient.getInstance();
    private TerminZooKeeperArbitrateEvent terminEvent;

    // private Map<Long, DistributedLock> locks = new ConcurrentHashMap<Long, DistributedLock>();

    /**
     * <pre>
     * 算法:
     * 1. 检查当前的Permit，阻塞等待其授权(解决Channel的pause状态处理)
     * 2. 开始阻塞获取符合条件的processId
     * 3. 检查当前的即时Permit状态 (在阻塞获取processId过程会出现一些error信号,process节点会被删除)
     * 4. 获取Select传递的EventData数据，添加next node信息后直接返回
     * </pre>
     */
    public EtlEventData await(Long pipelineId) throws InterruptedException {
        Assert.notNull(pipelineId);
        PermitMonitor permitMonitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        permitMonitor.waitForPermit();// 阻塞等待授权

        LoadStageListener loadStageListener = ArbitrateFactory.getInstance(pipelineId, LoadStageListener.class);
        Long processId = loadStageListener.waitForProcess(); // 符合条件的processId

        // DistributedLock lock = getLock(pipelineId);
        try {
            // 使用锁的理由：
            // 1. 针对双向同步时，其中一个方向出现了异常，需要发起另一端的关闭，此时对方正好在执行某个process的load
            // 2. 单向同步时，如果出现node节点异常，此时正常的节点正在执行某个process的load
            // 为避免因load无法中端引起的数据重复录入，所以针对load阶段添加分布式锁。在有process load过程中不允许进行pipeline关闭操作
            // lock.lock();

            ChannelStatus status = permitMonitor.getChannelPermit();
            if (status.isStart()) {// 即时查询一下当前的状态，状态随时可能会变
                // 根据pipelineId+processId构造对应的path
                String path = StagePathUtils.getTransformStage(pipelineId, processId);
                try {
                    byte[] data = zookeeper.readData(path);
                    return JsonUtils.unmarshalFromByte(data, EtlEventData.class);// 反序列化并返回
                } catch (ZkNoNodeException e) {
                    logger.error("pipeline[{}] processId[{}] is invalid , retry again", pipelineId, processId);
                    // try {
                    // lock.unlock();// 出现任何异常解除lock
                    // } catch (KeeperException e1) {
                    // // ignore
                    // }
                    return await(pipelineId);// /出现节点不存在，说明出现了error情况,递归调用重新获取一次
                } catch (ZkException e) {
                    throw e;
                }
            } else {
                logger.warn("pipelineId[{}] load ignore processId[{}] by status[{}]", new Object[] { pipelineId,
                        processId, status });
                // try {
                // lock.unlock();// 出现任何异常解除lock
                // } catch (KeeperException e) {
                // // ignore
                // }

                return await(pipelineId);// 出现rollback情况，递归调用重新获取一次，当前的processId可丢弃
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            // try {
            // lock.unlock();// 出现任何异常解除lock
            // } catch (KeeperException e1) {
            // // ignore
            // }

            throw new ArbitrateException(e);
        }
    }

    /**
     * <pre>
     * 算法:
     * 1. 创建对应的loaded节点,标志load已完成
     * </pre>
     * 
     * @param pipelineId 同步流id
     */
    public void single(EtlEventData data) {
        Assert.notNull(data);
        try {
            // String path = StagePathUtils.getLoadStage(data.getPipelineId(), data.getProcessId());
            // // 序列化
            // data.setEndTime(new Date().getTime());// 返回当前时间
            // byte[] bytes = JsonUtils.marshalToByte(data, SerializerFeature.WriteClassName);
            // try {
            // zookeeper.create(path, bytes, CreateMode.PERSISTENT);
            // } catch (NodeExistsException e) {
            // throw new ArbitrateException("Load_single", e.getMessage(), e);
            // } catch (KeeperException e) {
            // throw new ArbitrateException("Load_single", e.getMessage(), e);
            // } catch (InterruptedException e) {
            // // ignore
            // }

            data.setEndTime(new Date().getTime());// 返回当前时间

            // 调用Termin信号
            TerminEventData termin = new TerminEventData();
            termin.setPipelineId(data.getPipelineId());
            termin.setProcessId(data.getProcessId());
            termin.setStartTime(data.getStartTime());
            termin.setEndTime(data.getEndTime());
            termin.setFirstTime(data.getFirstTime());
            termin.setNumber(data.getNumber());
            termin.setBatchId(data.getBatchId());
            termin.setSize(data.getSize());
            termin.setExts(data.getExts());
            termin.setType(TerminType.NORMAL);
            termin.setCode("setl");
            termin.setDesc("");
            termin.setCurrNid(ArbitrateConfigUtils.getCurrentNid());
            terminEvent.single(termin);
        } finally {
            // // 最后一步释放锁
            // DistributedLock lock = getLock(data.getPipelineId());
            // try {
            // lock.unlock();// 解除lock
            // } catch (KeeperException e) {
            // throw new ArbitrateException("Load_single", e.getMessage(), e);
            // }
        }
    }

    /**
     * <pre>
     * 算法:
     * 1. load出现异常，解除load锁定，并发送对应的termin信号
     * </pre>
     * 
     * @param pipelineId 同步流id
     */
    public void release(Long pipelineId) {
        // DistributedLock lock = getLock(pipelineId);
        // try {
        // lock.unlock();// 解除lock
        // } catch (KeeperException e) {
        // throw new ArbitrateException("Load_single", e.getMessage(), e);
        // }
    }

    // private DistributedLock getLock(Long pipelineId) {
    // DistributedLock lock = locks.get(pipelineId);
    // if (lock == null) {
    // synchronized (locks) {
    // if (!locks.containsKey(pipelineId)) {
    // lock = new DistributedLock(StagePathUtils.getLoadLock(pipelineId));
    // locks.put(pipelineId, lock);
    // }
    // }
    // }
    //
    // return lock;
    // }

    // ======================== setter / getter ==========================

    public void setTerminEvent(TerminZooKeeperArbitrateEvent terminEvent) {
        this.terminEvent = terminEvent;
    }

}
