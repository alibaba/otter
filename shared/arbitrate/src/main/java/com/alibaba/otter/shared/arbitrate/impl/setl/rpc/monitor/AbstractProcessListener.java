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

package com.alibaba.otter.shared.arbitrate.impl.setl.rpc.monitor;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateLifeCycle;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.ReplyProcessQueue;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 抽取stage处理中一些共性的内容
 * 
 * @author jianghang 2011-9-21 下午02:16:17
 * @version 4.0.0
 */
public abstract class AbstractProcessListener extends ArbitrateLifeCycle implements ProcessListener {

    protected static final Logger logger    = LoggerFactory.getLogger(AbstractProcessListener.class);
    protected ZkClientx           zookeeper = ZooKeeperClient.getInstance();
    protected ReplyProcessQueue   replyProcessIds;                                                   // 有响应的processId列表
    protected ReentrantLock       lock      = new ReentrantLock();
    protected ProcessMonitor      processMonitor;

    public AbstractProcessListener(Long pipelineId){
        super(pipelineId);
        // 设置容量，必须大于并行度，这里设置为并行度的10倍，避免因并行度的运行时变化引起问题
        int size = ArbitrateConfigUtils.getParallelism(pipelineId) * 10;
        if (size < 100) {
            size = 100;
        }

        replyProcessIds = new ReplyProcessQueue(size);
        processMonitor = ArbitrateFactory.getInstance(pipelineId, ProcessMonitor.class);
        processMonitor.addListener(this);
        processMonitor.reload(); // 触发一下processChanged
    }

    public void processChanged(List<Long> processIds) {
        // 在运行过程中会出现Termin(rollback/restart/shutdown)等信号，仲裁器会删除当前运行的所有process
        // 因此需要删除之前已满足条件的队列记录，在具体的event处理时还会再对processId再做一次判断，是否已被废弃
        compareReply(processIds);
    }

    /**
     * 阻塞方法，获取对应可以被处理的processId，支持中断处理
     */
    public Long waitForProcess() throws InterruptedException {
        // take和history.put操作非原子，addReply操作时会出现并发问题，同一个processId插入两次
        Long processId = (Long) replyProcessIds.take();
        logger.debug("## {} get reply id [{}]", ClassUtils.getShortClassName(this.getClass()), processId);
        return processId;
    }

    protected synchronized void addReply(Long processId) {
        boolean isSuccessed = replyProcessIds.offer(processId);

        if (isSuccessed) {
            logger.debug("## {} add reply id [{}]", ClassUtils.getShortClassName(this.getClass()), processId);
        } else {
            logger.warn("## {} dup reply id [{}]", ClassUtils.getShortClassName(this.getClass()), processId);
        }
    }

    /**
     * 将当前的符合条件的processIds和当前的reply queue进行校对，剔除不在processIds里的内容
     */
    protected synchronized void compareReply(List<Long> processIds) {
        Object[] replyIds = replyProcessIds.toArray();
        for (Object replyId : replyIds) {
            if (processIds.contains((Long) replyId) == false) { // 判断reply id是否在当前processId列表中
                // 因为存在并发问题，如在执行Listener事件的同时，可能触发了process的创建，这时新建的processId会进入到reply队列中
                // 此时接受到的processIds变量为上一个版本的内容，所以会删除新建的process，导致整个通道被挂住
                if (CollectionUtils.isEmpty(processIds) == false) {
                    Long processId = processIds.get(0);
                    if (processId > (Long) replyId) { // 如果当前最小的processId都大于replyId, processId都是递增创建的
                        logger.info("## {} remove reply id [{}]", ClassUtils.getShortClassName(this.getClass()),
                                    (Long) replyId);
                        replyProcessIds.remove((Long) replyId);
                    }
                }
            }
        }
    }

    public void destory() {
        super.destory();
        logger.info("## destory pipeline[{}] , Listener[{}]", getPipelineId(),
                    ClassUtils.getShortClassName(this.getClass()));

        processMonitor.removeListener(this);
        replyProcessIds.clear();
    }

}
