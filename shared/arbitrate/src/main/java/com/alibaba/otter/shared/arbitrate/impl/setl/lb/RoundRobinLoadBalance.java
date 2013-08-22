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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.math.RandomUtils;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.common.model.config.node.Node;

/**
 * Round-Robin的负载均衡实现 <br/>
 * 
 * @author jianghang 2011-8-19 上午10:25:36
 */
public abstract class RoundRobinLoadBalance extends AbstractLoadBalance {

    private static final int MAX_ROUND    = 1000 * 1000;
    private AtomicInteger    round        = new AtomicInteger(0);
    private int              localPercent = 90;                  //local优先返回的权重，百分比

    public RoundRobinLoadBalance(Long pipelineId){
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
        if (existLocal && nodes.size() == 0) {//如果只有它自己
            return current;
        } else if (existLocal && RandomUtils.nextInt(100) < localPercent) {//计算一下百分比
            return current;
        } else {
            int number = round.incrementAndGet();
            if (number > MAX_ROUND) {
                number = round.getAndSet(0);
            }
            int index = (int) (number % nodes.size());
            return nodes.get(index);
        }

    }

    public void setLocalPercent(int localPercent) {
        this.localPercent = localPercent;
    }

}
