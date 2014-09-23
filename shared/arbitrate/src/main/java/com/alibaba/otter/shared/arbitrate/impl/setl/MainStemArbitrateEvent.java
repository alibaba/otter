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

package com.alibaba.otter.shared.arbitrate.impl.setl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.MainstemMonitor;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;

/**
 * 主导线程信号控制
 * 
 * @author jianghang 2011-8-9 下午05:16:16
 */
public class MainStemArbitrateEvent implements ArbitrateEvent {

    private static final Logger logger = LoggerFactory.getLogger(MainStemArbitrateEvent.class);

    /**
     * <pre>
     * 算法:
     * 1. 检查当前的Permit，阻塞等待其授权(解决Channel的pause状态处理)
     * 2. 尝试创建对应的mainStem节点(非持久化节点)
     *  a. 如果创建成功，则直接构造结果返回
     *  b. 如果创建失败，则关注该节点的exist信号. 继续执行步骤2
     * </pre>
     */
    public void await(Long pipelineId) throws InterruptedException {
        Assert.notNull(pipelineId);
        PermitMonitor permitMonitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        ChannelStatus status = permitMonitor.getChannelPermit(true);
        boolean isRuning = check(pipelineId);
        if (!status.isStart() && isRuning) {
            // 当前状态不为启动，强制设置为taking，下次授权启动后重新追数据
            MainStemEventData data = new MainStemEventData();
            data.setPipelineId(pipelineId);
            data.setStatus(MainStemEventData.Status.TAKEING);
            single(data);
            permitMonitor.waitForChannelPermit(); // 阻塞等待挂起
            return;
        } else if (status.isStart() && isRuning) {// 正常状态
            return;
        } else if (isRuning == false) {
            if (!status.isStart()) {
                permitMonitor.waitForChannelPermit(); // 阻塞等待挂起
            }
            MainstemMonitor mainstemMonitor = ArbitrateFactory.getInstance(pipelineId, MainstemMonitor.class);
            mainstemMonitor.waitForActive();// 等待自己成为active

            status = permitMonitor.getChannelPermit(false);
            if (status.isStart()) {
                return;
            } else {
                logger.info("pipelineId[{}] mainstem ignore by status[{}]", new Object[] { pipelineId, status });
                await(pipelineId);// 重新进行check一次
            }
        }

    }

    /**
     * 检查下当前的mainstem是否可以运行(在await获取成功后，每次执行业务之前进行check)
     */
    public boolean check(Long pipelineId) {
        MainstemMonitor mainstemMonitor = ArbitrateFactory.getInstance(pipelineId, MainstemMonitor.class);
        return mainstemMonitor.check();
    }

    /**
     * 更新mainStem的同步状态数据
     */
    public void single(MainStemEventData data) {
        MainstemMonitor mainstemMonitor = ArbitrateFactory.getInstance(data.getPipelineId(), MainstemMonitor.class);
        mainstemMonitor.single(data);
    }

    /**
     * 释放mainStem的节点，重新选择工作节点
     */
    public void release(Long pipelineId) {
        MainstemMonitor mainstemMonitor = ArbitrateFactory.getInstance(pipelineId, MainstemMonitor.class);
        mainstemMonitor.releaseMainstem();
    }

}
