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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.I0Itec.zkclient.IZkChildListener;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateLifeCycle;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.Monitor;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.MonitorScheduler;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * process节点列表的监控，{@linkplain stageMonitor}的简化版本，只关注了process节点的变化，不再关心process的子节点stage的变化，减少了对zk watcher的依赖
 * 
 * <pre>
 * 1. 获取当前的process列表，用于控制并行度
 * 2. 获取当前的最小的processId，用于控制load的按顺序载入
 * </pre>
 * 
 * @author jianghang 2012-9-28 下午09:35:00
 * @version 4.1.0
 */
public class ProcessMonitor extends ArbitrateLifeCycle implements Monitor {

    private static final Logger   logger            = LoggerFactory.getLogger(ProcessMonitor.class);

    private ExecutorService       arbitrateExecutor;
    private ZkClientx             zookeeper         = ZooKeeperClient.getInstance();
    private volatile List<Long>   currentProcessIds = new ArrayList<Long>();                                         // 当前的处于监控中的processId列表
    private List<ProcessListener> listeners         = Collections.synchronizedList(new ArrayList<ProcessListener>());
    private IZkChildListener      processListener;

    public ProcessMonitor(Long pipelineId){
        super(pipelineId);
        processListener = new IZkChildListener() {

            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                if (currentChilds != null) {
                    initProcess(currentChilds);
                }
            }
        };

        String path = StagePathUtils.getProcessRoot(getPipelineId());
        List<String> childs = zookeeper.subscribeChildChanges(path, processListener);
        initProcess(childs);
        // syncStage();
        MonitorScheduler.register(this);
    }

    public void reload() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("## reload Stage pipeline[{}]", getPipelineId());
            }

            initProcess();
        } catch (Exception cause) {
        }
    }

    /**
     * 获取当前的process id列表
     */
    public List<Long> getCurrentProcessIds() {
        return getCurrentProcessIds(false);
    }

    /**
     * 获取当前的process id列表，指定是否强制刷新
     */
    public List<Long> getCurrentProcessIds(boolean reload) {
        if (reload) {
            reload();
        }

        return currentProcessIds;
    }

    public void destory() {
        super.destory();
        if (logger.isDebugEnabled()) {
            logger.debug("## destory process pipeline[{}]", getPipelineId());
        }

        this.listeners.clear();
        String path = StagePathUtils.getProcessRoot(getPipelineId());
        zookeeper.unsubscribeChildChanges(path, processListener);
        MonitorScheduler.unRegister(this);
    }

    /**
     * 获取zk列表数据，无须同步处理
     */
    private void initProcess() {
        // 1. 根据pipelineId构造对应的path
        String path = StagePathUtils.getProcessRoot(getPipelineId());
        // 2. 获取当前的所有process列表
        List<String> currentProcesses = zookeeper.getChildren(path);
        initProcess(currentProcesses);
    }

    /**
     * 重新获取最新的process列表
     */
    private void initProcess(List<String> currentProcesses) {
        // 3. 循环处理每个process
        List<Long> processIds = new ArrayList<Long>();
        for (String process : currentProcesses) {
            processIds.add(StagePathUtils.getProcessId(process));
        }
        Collections.sort(processIds); // 排序一下
        if (logger.isDebugEnabled()) {
            logger.debug("pipeline[{}] old processIds{},current processIds{}", new Object[] { getPipelineId(),
                    currentProcessIds, processIds });
        }

        // if (!processIds.equals(currentProcessIds) || currentProcessIds.isEmpty()) {// 不相同，说明有变化
        processChanged(processIds);// 通知变化
        // }

        currentProcessIds = processIds; // 切换引用，需设置为volatile保证线程安全&可见性
    }

    // ======================== listener处理 ======================

    public void addListener(ProcessListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("## pipeline[{}] add listener [{}]", getPipelineId(),
                         ClassUtils.getShortClassName(listener.getClass()));
        }

        this.listeners.add(listener);
    }

    public void removeListener(ProcessListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("## pipeline[{}] remove listener [{}]", getPipelineId(),
                         ClassUtils.getShortClassName(listener.getClass()));
        }

        this.listeners.remove(listener);
    }

    private void processChanged(final List<Long> processIds) {
        for (final ProcessListener listener : listeners) {
            // 异步处理
            arbitrateExecutor.submit(new Runnable() {

                public void run() {
                    MDC.put(ArbitrateConstants.splitPipelineLogFileKey, String.valueOf(getPipelineId()));
                    listener.processChanged(processIds);
                }
            });
        }
    }

    public void setArbitrateExecutor(ExecutorService arbitrateExecutor) {
        this.arbitrateExecutor = arbitrateExecutor;
    }

}
