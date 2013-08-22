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
import com.alibaba.otter.shared.arbitrate.impl.setl.TransformArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.monitor.TransformStageListener;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 关注extracted节点，创建transformed节点
 * 
 * @author jianghang 2011-8-9 下午05:10:50
 */
public class TransformZooKeeperArbitrateEvent implements TransformArbitrateEvent {

    private static final Logger logger    = LoggerFactory.getLogger(TransformZooKeeperArbitrateEvent.class);
    private ZkClientx           zookeeper = ZooKeeperClient.getInstance();

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

        TransformStageListener transformStageListener = ArbitrateFactory.getInstance(pipelineId,
                                                                                     TransformStageListener.class);
        Long processId = transformStageListener.waitForProcess(); // 符合条件的processId

        ChannelStatus status = permitMonitor.getChannelPermit();
        if (status.isStart()) {// 即时查询一下当前的状态，状态随时可能会变
            // 根据pipelineId+processId构造对应的path
            String path = StagePathUtils.getExtractStage(pipelineId, processId);

            try {
                byte[] data = zookeeper.readData(path);
                EtlEventData eventData = JsonUtils.unmarshalFromByte(data, EtlEventData.class);
                eventData.setNextNid(ArbitrateConfigUtils.getCurrentNid());// 下一个节点信息即为自己
                return eventData;// 只有这一条路返回
            } catch (ZkNoNodeException e) {
                logger.error("pipeline[{}] processId[{}] is invalid , retry again", pipelineId, processId);
                return await(pipelineId);// /出现节点不存在，说明出现了error情况,递归调用重新获取一次
            } catch (ZkException e) {
                throw new ArbitrateException("transform_await", e.getMessage(), e);
            }
        } else {
            logger.info("pipelineId[{}] transform ignore processId[{}] by status[{}]", new Object[] { pipelineId,
                    processId, status });
            return await(pipelineId);// 递归调用
        }
    }

    /**
     * <pre>
     * 算法:
     * 1. 创建对应的transformed节点,标志transform已完成
     * </pre>
     * 
     * @param pipelineId 同步流id
     */
    public void single(EtlEventData data) {
        Assert.notNull(data);
        String path = StagePathUtils.getTransformStage(data.getPipelineId(), data.getProcessId());
        data.setCurrNid(ArbitrateConfigUtils.getCurrentNid());
        // 序列化
        byte[] bytes = JsonUtils.marshalToByte(data, SerializerFeature.WriteClassName);
        try {
            zookeeper.create(path, bytes, CreateMode.PERSISTENT);
        } catch (ZkNoNodeException e) {
            // process节点不存在，出现了rollback/shutdown操作，直接忽略
            logger.warn("pipelineId[{}] transform ignore processId[{}] single by data:{}",
                        new Object[] { data.getPipelineId(), data.getProcessId(), data });
        } catch (ZkNodeExistsException e) {
            // process节点已存在，出现了ConnectionLoss retry操作
            logger.warn("pipelineId[{}] transform ignore processId[{}] single by data:{}",
                        new Object[] { data.getPipelineId(), data.getProcessId(), data });
        } catch (ZkException e) {
            throw new ArbitrateException("transform_single", e.getMessage(), e);
        }
    }

}
