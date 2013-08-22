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

package com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.termin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.I0Itec.zkclient.exception.ZkException;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.lock.DistributedLock;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 抽取异常信号的公共处理，termin的chain处理
 * 
 * @author jianghang 2011-9-27 下午04:58:06
 * @version 4.0.0
 */
public class ErrorTerminProcess implements TerminProcess {

    protected static final Logger    logger    = LoggerFactory.getLogger(ErrorTerminProcess.class);
    protected ZkClientx              zookeeper = ZooKeeperClient.getInstance();
    protected ArbitrateManageService arbitrateManageService;
    protected NormalTerminProcess    normalTerminProcess;

    public boolean process(TerminEventData data) {
        DistributedLock lock = new DistributedLock(StagePathUtils.getLoadLock(data.getPipelineId()));
        try {
            boolean locked = lock.tryLock();// 尝试进行锁定，等待当前的load操作完成
            if (!locked) {
                return false;
            }
            processChain(data);
            return true;
        } catch (KeeperException e) {
            throw new ArbitrateException("Termin_process", e);
        } finally {
            try {
                lock.unlock();// 马上进行释放
            } catch (KeeperException e1) {
                // ignore
            }
        }
    }

    public void processChain(TerminEventData data) {
        // 关闭对应的服务
        Long pipelineId = data.getPipelineId();

        // 清理对应的process
        String processRoot = StagePathUtils.getProcessRoot(pipelineId);
        try {
            List<String> processNodes = zookeeper.getChildren(processRoot);
            // 3. 循环处理每个process
            List<Long> processIds = new ArrayList<Long>();
            for (String process : processNodes) {
                processIds.add(StagePathUtils.getProcessId(process));
            }
            Collections.sort(processIds); // 排序一下

            Long processId = data.getProcessId();
            if (processId != null) {// 可能为空
                normalTerminProcess.process(data);
            }

            for (Long currProcessId : processIds) {
                if (processId != null && currProcessId <= processId) {
                    continue;
                }

                // 发送给最小的一个process的termin信号，进行链式的触发
                data.setProcessId(currProcessId);
                processChain(data); // 处理异常信息
                break;
            }

        } catch (ZkException e) {
            throw new ArbitrateException("Termin_process", e);
        }
    }

    // =================== setter / getter ====================

    public void setArbitrateManageService(ArbitrateManageService arbitrateManageService) {
        this.arbitrateManageService = arbitrateManageService;
    }

    public void setNormalTerminProcess(NormalTerminProcess normalTerminProcess) {
        this.normalTerminProcess = normalTerminProcess;
    }

}
