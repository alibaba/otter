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

import java.util.List;

import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.utils.JsonUtils;

/**
 * 处理transofrm模块节点的监控
 * 
 * <pre>
 * 监控内容：
 *  1. 某个process的stage节点发生变化后，判断extracted节点是否已经准备完成
 * </pre>
 * 
 * @author jianghang 2011-9-21 下午02:20:30
 * @version 4.0.0
 */
public class TransformStageListener extends AbstractStageListener implements StageListener {

    private static final String currentNode = ArbitrateConstants.NODE_TRANSFORMED;
    private static final String prevNode    = ArbitrateConstants.NODE_EXTRACTED;

    public TransformStageListener(Long pipelineId){
        super(pipelineId);
    }

    public void processChanged(List<Long> processIds) {
        // do nothing
    }

    public void stageChannged(Long processId, List<String> stageNodes) {
        try {
            // 1. 根据pipelineId+processId构造对应的path
            String path = StagePathUtils.getProcess(getPipelineId(), processId);
            // 2.1 判断是否存在了error节点,end节点或者current节点
            if (stageNodes.contains(currentNode)) {
                if (replyProcessIds.remove(processId)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("## remove reply id [{}]", processId);
                    }
                }
                return;// 不需要监听了
            }

            if (replyProcessIds.contains(processId)) {
                return;// 避免重复处理
            }

            // 2.2 判断是否存在了prev节点
            if (stageNodes.contains(prevNode)) {
                // 2.2.1 获取上一个节点的next node节点信息
                byte[] data = zookeeper.readData(path + "/" + prevNode);
                EtlEventData eventData = JsonUtils.unmarshalFromByte(data, EtlEventData.class);
                if (eventData.getNextNid().equals(ArbitrateConfigUtils.getCurrentNid())) {
                    addReply(processId);// 添加到返回队列,唤醒wait阻塞
                }
            }
        } catch (ZkNoNodeException e) {
            // 出现节点不存在，说明出现了error情况
        } catch (ZkException e) {
            logger.error("TransformStageListener", e);
        }
    }

}
