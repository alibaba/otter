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

import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
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
import com.alibaba.otter.shared.arbitrate.impl.setl.ExtractArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.lb.LoadBalanceFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.ExtractStageListener;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 关注selected节点，创建extracted节点
 * 
 * @author jianghang 2011-8-9 下午05:10:50
 */
public class ExtractZooKeeperArbitrateEvent implements ExtractArbitrateEvent {

    private static final Logger logger    = LoggerFactory.getLogger(ExtractZooKeeperArbitrateEvent.class);
    private ZkClientx           zookeeper = ZooKeeperClient.getInstance();

    // private TerminArbitrateEvent terminEvent;

    /**
     * <pre>
     * 算法:
     * 1. 检查当前的Permit，阻塞等待其授权(解决Channel的pause状态处理)
     * 2. 开始阻塞获取符合条件的processId
     * 3. 检查当前的即时Permit状态 (在阻塞获取processId过程会出现一些error信号,process节点会被删除)
     * 4. 获取Select传递的EventData数据，添加next node信息后直接返回
     * </pre>
     * 
     * @return
     */
    public EtlEventData await(Long pipelineId) throws InterruptedException {
        Assert.notNull(pipelineId);
        PermitMonitor permitMonitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        permitMonitor.waitForPermit();// 阻塞等待授权

        ExtractStageListener extractStageListener = ArbitrateFactory.getInstance(pipelineId, ExtractStageListener.class);
        Long processId = extractStageListener.waitForProcess(); // 符合条件的processId

        ChannelStatus status = permitMonitor.getChannelPermit();
        if (status.isStart()) {// 即时查询一下当前的状态，状态随时可能会变
            // 根据pipelineId+processId构造对应的path
            String path = StagePathUtils.getSelectStage(pipelineId, processId);

            try {
                byte[] data = zookeeper.readData(path);
                EtlEventData eventData = JsonUtils.unmarshalFromByte(data, EtlEventData.class);

                Node node = LoadBalanceFactory.getNextTransformNode(pipelineId);// 获取下一个处理节点信息
                if (node == null) {// 没有后端节点
                    // TerminEventData termin = new TerminEventData();
                    // termin.setPipelineId(pipelineId);
                    // termin.setType(TerminType.ROLLBACK);
                    // termin.setCode("no_node");
                    // termin.setDesc(MessageFormat.format("pipeline[{}] extract stage has no node!", pipelineId));
                    // terminEvent.single(termin);
                    throw new ArbitrateException("Extract_single", "no next node");
                } else {
                    eventData.setNextNid(node.getId());
                    return eventData;// 只有这一条路返回
                }
            } catch (ZkNoNodeException e) {
                logger.error("pipeline[{}] processId[{}] is invalid , retry again", pipelineId, processId);
                return await(pipelineId);// /出现节点不存在，说明出现了error情况,递归调用重新获取一次
            } catch (ZkException e) {
                throw new ArbitrateException("Extract_await", e.getMessage(), e);
            }
        } else {
            logger.warn("pipelineId[{}] extract ignore processId[{}] by status[{}]", new Object[] { pipelineId,
                    processId, status });
            return await(pipelineId);// 递归调用
        }

    }

    /**
     * <pre>
     * 算法:
     * 1. 创建对应的extracted节点,标志extract已完成
     * </pre>
     * 
     * @param pipelineId 同步流id
     */
    public void single(EtlEventData data) {
        Assert.notNull(data);
        String path = StagePathUtils.getExtractStage(data.getPipelineId(), data.getProcessId());
        data.setCurrNid(ArbitrateConfigUtils.getCurrentNid());
        // 序列化
        byte[] bytes = JsonUtils.marshalToByte(data, SerializerFeature.WriteClassName);
        try {
            zookeeper.create(path, bytes, CreateMode.PERSISTENT);
        } catch (ZkNoNodeException e) {
            // process节点不存在，出现了rollback/shutdown操作，直接忽略
            logger.warn("pipelineId[{}] extract ignore processId[{}] single by data:{}",
                        new Object[] { data.getPipelineId(), data.getProcessId(), data });
        } catch (ZkNodeExistsException e) {
            // process节点已存在，出现了ConnectionLoss retry操作
            logger.warn("pipelineId[{}] extract ignore processId[{}] single by data:{}",
                        new Object[] { data.getPipelineId(), data.getProcessId(), data });
        } catch (ZkInterruptedException e) {
            // ignore
        } catch (ZkException e) {
            throw new ArbitrateException("Extract_single", e.getMessage(), e);
        }
    }

    // public void setTerminEvent(TerminArbitrateEvent terminEvent) {
    // this.terminEvent = terminEvent;
    // }
}
