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
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.SelectArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.lb.LoadBalanceFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.SelectStageListener;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.model.ProcessNodeEventData;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter.ArbitrateMode;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 基于zookeeper的仲裁器调度
 * 
 * @author jianghang 2011-8-9 下午05:10:50
 */
public class SelectZooKeeperArbitrateEvent implements SelectArbitrateEvent {

    private static final Logger logger    = LoggerFactory.getLogger(SelectZooKeeperArbitrateEvent.class);
    private ZkClientx           zookeeper = ZooKeeperClient.getInstance();

    // private TerminArbitrateEvent terminEvent;

    /**
     * <pre>
     * 算法:
     * 1. 检查当前的Permit，阻塞等待其授权(解决Channel的pause状态处理)
     * 2. 开始阻塞获取符合条件的processId，创建空的EventData对象,添加next node信息后直接返回
     * </pre>
     */
    public EtlEventData await(Long pipelineId) throws InterruptedException {
        Assert.notNull(pipelineId);

        PermitMonitor permitMonitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        permitMonitor.waitForPermit();// 阻塞等待授权

        SelectStageListener selectStageListener = ArbitrateFactory.getInstance(pipelineId, SelectStageListener.class);
        Long processId = selectStageListener.waitForProcess(); // 符合条件的processId

        ChannelStatus status = permitMonitor.getChannelPermit();
        if (status.isStart()) {// 即时查询一下当前的状态，状态随时可能会变

            try {
                EtlEventData eventData = new EtlEventData();
                eventData.setPipelineId(pipelineId);
                eventData.setProcessId(processId);
                eventData.setStartTime(new Date().getTime());// 返回当前时间

                Node node = LoadBalanceFactory.getNextExtractNode(pipelineId);// 获取下一个处理节点信息
                if (node == null) {// 没有后端节点
                    // TerminEventData termin = new TerminEventData();
                    // termin.setPipelineId(pipelineId);
                    // termin.setType(TerminType.ROLLBACK);
                    // termin.setCode("no_node");
                    // termin.setDesc(MessageFormat.format("pipeline[{}] extract stage has no node!", pipelineId));
                    // terminEvent.single(termin);
                    throw new ArbitrateException("Select_single", "no next node");
                } else {
                    eventData.setNextNid(node.getId());
                    markUsed(eventData); // 标记为已使用
                    return eventData;// 只有这一条路返回
                }
            } catch (ZkNoNodeException e) {
                logger.error("pipeline[{}] processId[{}] is invalid , retry again", pipelineId, processId);
                return await(pipelineId);// /出现节点不存在，说明出现了error情况,递归调用重新获取一次
            } catch (ZkException e) {
                throw new ArbitrateException("Select_await", e.getMessage(), e);
            }
        } else {
            logger.warn("pipelineId[{}] select ignore processId[{}] by status[{}]", new Object[] { pipelineId,
                    processId, status });
            // add by ljh 2013-02-01
            // 遇到一个bug:
            // a. 某台机器发起了一个RESTART指令，然后开始删除process列表
            // b. 此时另一个台机器(select工作节点)，并没有收到PAUSE的推送，导致还会再创建一个process节点
            // c. 后续收到PAUSE指令后，丢弃了processId，就出现了unused的processId
            // 这里删除了，要考虑一个问题，就是和restart指令在并行删除同一个processId时的并发考虑，目前来看没问题
            String path = StagePathUtils.getProcess(pipelineId, processId);
            zookeeper.delete(path); // 忽略删除失败
            return await(pipelineId);// 递归调用
        }
    }

    /**
     * <pre>
     * 算法:
     * 1. 创建对应的selected节点,标志selected已完成
     * </pre>
     * 
     * @param pipelineId 同步流id
     */
    public void single(EtlEventData data) {
        Assert.notNull(data);
        String path = StagePathUtils.getSelectStage(data.getPipelineId(), data.getProcessId());
        data.setCurrNid(ArbitrateConfigUtils.getCurrentNid());
        // 序列化
        byte[] bytes = JsonUtils.marshalToByte(data, SerializerFeature.WriteClassName);
        try {
            zookeeper.create(path, bytes, CreateMode.PERSISTENT);
        } catch (ZkNoNodeException e) {
            // process节点不存在，出现了rollback/shutdown操作，直接忽略
            logger.warn("pipelineId[{}] select ignore processId[{}] single by data:{}",
                        new Object[] { data.getPipelineId(), data.getProcessId(), data });
        } catch (ZkNodeExistsException e) {
            // process节点已存在，出现了ConnectionLoss retry操作
            logger.warn("pipelineId[{}] select ignore processId[{}] single by data:{}",
                        new Object[] { data.getPipelineId(), data.getProcessId(), data });
        } catch (ZkException e) {
            throw new ArbitrateException("Select_single", e.getMessage(), e);
        }

    }

    /**
     * 标记一下当前process为已使用
     */
    private void markUsed(EtlEventData data) throws ZkNoNodeException, ZkException {
        String path = StagePathUtils.getProcess(data.getPipelineId(), data.getProcessId());
        // 序列化
        ProcessNodeEventData eventData = new ProcessNodeEventData();
        Long nid = ArbitrateConfigUtils.getCurrentNid();
        eventData.setNid(nid);
        eventData.setStatus(ProcessNodeEventData.Status.USED);// 标记为已使用
        eventData.setMode(ArbitrateMode.ZOOKEEPER);// 直接声明为zookeeper模式
        byte[] bytes = JsonUtils.marshalToByte(eventData);
        zookeeper.writeData(path, bytes);
    }

    // public void setTerminEvent(TerminArbitrateEvent terminEvent) {
    // this.terminEvent = terminEvent;
    // }

}
