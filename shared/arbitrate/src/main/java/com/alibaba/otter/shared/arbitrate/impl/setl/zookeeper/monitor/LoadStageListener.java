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
 * 处理load模块节点的监控
 * 
 * <pre>
 * 监控内容：
 *  1. 某个process的stage节点发生变化后，判断transform节点是否已经准备完成，并判断当前是否为最小的process
 *  2. process发生变化后，检测当前的最小processId是否有变化，有变化则触发检查是否可以进行load操作
 * </pre>
 * 
 * @author jianghang 2011-9-21 下午02:20:52
 * @version 4.0.0
 */
public class LoadStageListener extends AbstractStageListener implements StageListener {

    // private static final String currentNode = ArbitrateConstants.NODE_LOADED;
    private static final String prevNode = ArbitrateConstants.NODE_TRANSFORMED;

    public LoadStageListener(Long pipelineId){
        super(pipelineId);
    }

    public void processChanged(List<Long> processIds) {
        // nothing
    }

    public void stageChannged(Long processId, List<String> stageNodes) {
        try {
            // 1. 根据pipelineId+processId构造对应的path
            String path = StagePathUtils.getProcess(getPipelineId(), processId);
            // 2.1 判断是否存在了error节点,end节点或者current节点
            // if (stageNodes.contains(currentNode)) {
            // if (replyProcessIds.remove(processId)) {
            // if (logger.isDebugEnabled()) {
            // logger.debug("## remove reply id [{}]", processId);
            // }
            // }
            // return;// 不需要监听了
            // }

            if (replyProcessIds.contains(processId)) {
                return;// 避免重复处理
            }

            // 2.2 判断是否存在了prev节点
            if (stageNodes.contains(prevNode)) {
                // 2.2.1 获取上一个节点的next node节点信息
                byte[] data = zookeeper.readData(path + "/" + prevNode);
                EtlEventData eventData = JsonUtils.unmarshalFromByte(data, EtlEventData.class);
                if (eventData.getNextNid().equals(ArbitrateConfigUtils.getCurrentNid())) {
                    List<Long> currentProcessIds = stageMonitor.getCurrentProcessIds(false);
                    if (currentProcessIds.contains(processId) && currentProcessIds.get(0).equals(processId)) {
                        // 判断是否是当前最小的processId节点，轮到自己处理了
                        addReply(processId);// 添加到返回队列,唤醒wait阻塞
                    }

                }
            }
        } catch (ZkNoNodeException e) {
            // 出现节点不存在，说明出现了error情况
        } catch (ZkException e) {
            logger.error("LoadStageListener", e);
        }
    }
}
