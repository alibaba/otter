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

package com.alibaba.otter.shared.arbitrate.impl.setl.lb;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.math.RandomUtils;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.common.model.config.node.Node;

/**
 * 提供一种固定粘性的{@linkplain LoadBalance}的机制，因为多个load落在同一个jvm，可以减少通过zookeeper的仲裁调度交互
 * 
 * @author jianghang 2013-2-25 下午10:51:56
 * @version 4.1.7
 */
public abstract class StickLoadBalance extends AbstractLoadBalance implements LoadBalance {

    private int        stickPercent   = 100;              // 固定返回某一个节点的百分比
    private long       lastNid        = -1;               // 上一次选择的节点
    private int        randomThresold = 100;              // 超过100个批次后重新选择
    private AtomicLong stickCount     = new AtomicLong(0);

    public StickLoadBalance(Long pipelineId){
        super(pipelineId);
    }

    public Node next() throws InterruptedException {
        List<Node> nodes = getAliveNodes();

        if (nodes == null || nodes.size() == 0) {
            return null;
        }

        Long nid = ArbitrateConfigUtils.getCurrentNid();
        Node current = new Node();
        current.setId(nid);

        // 判断一下是否优先返回local
        boolean existLocal = nodes.remove(current);
        if (existLocal && nodes.size() == 0) {// 如果只有它自己
            return current;
        } else if (existLocal && RandomUtils.nextInt(100) <= stickPercent) {// 计算一下百分比
            return current;
        } else {
            for (Node node : nodes) {
                if (node.getId().equals(lastNid) && RandomUtils.nextInt(100) <= stickPercent) {
                    lastNid = node.getId();
                    long count = stickCount.incrementAndGet();
                    if (count > randomThresold) {
                        lastNid = -1; // 进入下一轮的重新选择，避免挂死一个节点
                        stickCount.set(0);
                    }
                    return node;
                }
            }

            // 如果没找到对应lastNid对应的信息，可能节点挂了，则随机选一个
            int index = RandomUtils.nextInt(nodes.size());
            Node node = nodes.get(index);
            lastNid = node.getId();
            return node;
        }

    }

    public void setStickPercent(int stickPercent) {
        this.stickPercent = stickPercent;
    }

    public void setRandomThresold(int randomThresold) {
        this.randomThresold = randomThresold;
    }

}
