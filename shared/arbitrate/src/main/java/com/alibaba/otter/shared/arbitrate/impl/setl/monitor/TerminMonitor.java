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

package com.alibaba.otter.shared.arbitrate.impl.setl.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateLifeCycle;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.TerminProcessQueue;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 结束信号的监听
 * 
 * @author jianghang 2011-9-26 上午11:31:50
 * @version 4.0.0
 */
public class TerminMonitor extends ArbitrateLifeCycle implements Monitor {

    private static final Logger logger         = LoggerFactory.getLogger(TerminMonitor.class);

    private ZkClientx           zookeeper      = ZooKeeperClient.getInstance();
    private TerminProcessQueue  waitProcessIds = new TerminProcessQueue();                    // 记录对应的终结信号数据，从小到大的排序
    private IZkChildListener    childListener;

    public TerminMonitor(Long pipelineId){
        super(pipelineId);
        childListener = new IZkChildListener() {

            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                if (currentChilds != null) {
                    initTermin(currentChilds);
                }
            }
        };

        String path = StagePathUtils.getTerminRoot(getPipelineId());
        List<String> childs = zookeeper.subscribeChildChanges(path, childListener);
        initTermin(childs);
        MonitorScheduler.register(this);
    }

    public void reload() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("## reload termin pipeline[{}]", getPipelineId());
            }

            initTermin();
        } catch (Exception e) {
            // ignore
        }
    }

    public void destory() {
        super.destory();
        if (logger.isDebugEnabled()) {
            logger.debug("## destory termin pipeline[{}]", getPipelineId());
        }

        String path = StagePathUtils.getTerminRoot(getPipelineId());
        zookeeper.unsubscribeChildChanges(path, childListener);
        MonitorScheduler.unRegister(this);
        waitProcessIds.clear();
    }

    /**
     * 阻塞获取对应的process的termin事件
     */
    public Long waitForProcess() throws InterruptedException {
        Long processId = waitProcessIds.peek();
        if (logger.isDebugEnabled()) {
            logger.debug("## {} get termin id [{}]", getPipelineId(), processId);
        }
        return processId;
    }

    /**
     * @return 当前待处理的termin信号的总数
     */
    public int size() {
        return waitProcessIds.size();
    }

    /**
     * 提交termin的ack信息，物理移除termin
     */
    public boolean ack(Long processId) {
        boolean result = waitProcessIds.ack();
        if (logger.isDebugEnabled()) {
            logger.debug("## {} ack termin id [{}]", getPipelineId(), processId);
        }
        return result;
    }

    // ================ 状态数据同步 ================

    private void initTermin() {
        String path = StagePathUtils.getTerminRoot(getPipelineId());
        List<String> termins = zookeeper.getChildren(path);
        initTermin(termins);
    }

    private synchronized void initTermin(List<String> termins) {
        if (CollectionUtils.isEmpty(termins)) {
            return;
        }

        List<Long> processIds = new ArrayList<Long>(termins.size());
        for (String termin : termins) {
            processIds.add(StagePathUtils.getProcessId(termin));
        }
        // 排序一下
        Collections.sort(processIds);
        for (Long processId : processIds) {
            boolean successed = waitProcessIds.offer(processId);
            if (successed && logger.isDebugEnabled()) {
                logger.debug("## {} add termin id [{}]", getPipelineId(), processId);
            }
        }
    }

    // private void syncTermin() {
    // String path = null;
    // try {
    // path = StagePathUtils.getTerminRoot(getPipelineId());
    // List<String> termins = zookeeper.getChildren(path, new AsyncWatcher() {
    //
    // public void asyncProcess(WatchedEvent event) {
    // MDC.put(ArbitrateConstants.splitPipelineLogFileKey, String.valueOf(getPipelineId()));
    // if (isStop()) {
    // return;
    // }
    //
    // // 出现session expired/connection losscase下，会触发所有的watcher响应，同时老的watcher会继续保留，所以会导致出现多次watcher响应
    // boolean dataChanged = event.getType() == EventType.NodeDataChanged
    // || event.getType() == EventType.NodeDeleted
    // || event.getType() == EventType.NodeCreated
    // || event.getType() == EventType.NodeChildrenChanged;
    // if (dataChanged) {
    // syncTermin();// 继续关注
    // }
    // }
    // });
    //
    // // watcher 挂载需要时间，先检查一遍
    // initTermin(termins);
    // } catch (KeeperException e) {
    // syncTermin();// 挂失败了，重新添加一个
    // } catch (InterruptedException e) {
    // // ignore
    // }
    // }
}
