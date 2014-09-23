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

import org.apache.commons.lang.math.RandomUtils;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.common.model.config.node.Node;

/**
 * Random的负载均衡实现 <br/>
 * 
 * @author jianghang 2011-9-16 下午07:12:27
 * @version 4.0.0
 */
public abstract class RandomLoadBalance extends AbstractLoadBalance implements LoadBalance {

    private int localPercent = 90; //local优先返回的权重，百分比

    public RandomLoadBalance(Long pipelineId){
        super(pipelineId);
    }

    @Override
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
            int index = RandomUtils.nextInt(nodes.size());
            return nodes.get(index);
        }

    }

    public void setLocalPercent(int localPercent) {
        this.localPercent = localPercent;
    }

}
