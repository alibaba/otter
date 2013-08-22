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

package com.alibaba.otter.shared.arbitrate.impl.setl.fastrpc;

import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateLifeCycle;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.listener.PermitListener;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.enums.StageType;

/**
 * 基于memory + rpc的调度模式的一个整合版，不依赖于zookeeper的process创建
 * 
 * <pre>
 * 大致算法:
 * 1. 基于内存方式创建一个顺序processId，正常运行过程中不可重复，出现Rollback/Stop操作后可重复
 * 2. 判断最小id的方法：
 *    a. 需要记录上一次成功load的processId，如果当前id为该processId + 1的话，则为最小id. 
 *    b. 当上一次load的processId为0时，代表第一次启动，数字id 1为最小id
 *    前提：
 *      i. 每次启动，processId一定是从1开始分配，并且必须是连续的id分配
 * 3. 运行时如何停止：
 *    a. 首先修改channel status状态，通过PermitMonitor通知所有节点，"尽可能"hold住所有s/e/t/l调度. 因为zookeepr watcher通知存在延迟，高峰美国有5秒的延迟
 *    b. 每个{@linkplain FastRpcStageController}实例，监听Permit的变化，发现ChannelStatus出现Rollback/Stop情况，立即销毁
 *    前提：
 *      i. zookeeper watcher推送存在不确定性，多台机器推送有先后，其中一台重建状态，另一台可能还处于老状态，
 * </pre>
 * 
 * @author jianghang 2013-2-28 下午10:15:20
 * @version 4.1.7
 */
public class FastRpcStageController extends ArbitrateLifeCycle implements PermitListener {

    public FastRpcStageController(Long pipelineId){
        super(pipelineId);
    }

    public synchronized boolean single(StageType stage, EtlEventData etlEventData) {
        return true;
    }

    public void processChanged(boolean isPermit) {

    }
}
