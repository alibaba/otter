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

import java.util.Collections;
import java.util.List;

import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.zookeeper.CreateMode;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StageComparator;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.arbitrate.model.ProcessNodeEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 正常的结束信号处理
 * 
 * <pre>
 * 特殊说明： 告知以后所有维护此代码的人，多看看写的一些注释，这个类并发问题太多了，踩了好多坑.
 * 1. normal和shutdown/rollback操作存在并发性，shutdown/rollback有互斥锁，所以不会有并发。 (normal加互斥锁代价高，不合算)
 * 2. 执行shutdown/rollback操作时，虽然是先修改channel状态，但是normal可能还会多跑一个stage，这时执行process删除时，normal又会创建一个stage，导致删除失败，需要重试
 * 3. 需要先创建termin，再删除process (以前遇到并发，删除了一个process，在创建termin的过程，另一个process流程提前完成了这一系列动作，导致termin顺序不对)
 * 4. normal和shutdown/rollback的并发操作，会导致重复创建termin. (两个操作都发现要创建termin，一个先完成create，并且被消费者消费了termin并删除了，另一个再create时也能成功了，导致出现重复) 
 * 
 * 大致想到了这么几点，没事就别改这个类了，啥并发问题都有，(建议多用用新版的canal，对termin信号顺序/无重复没有这么强要求，客户端多retry几次有点重复数据，事情也就不会那么复杂) 
 * write at 2012-09-06 by jianghang.loujh
 * </pre>
 * 
 * @author jianghang 2011-9-26 下午01:52:53
 * @version 4.0.0
 */
public class NormalTerminProcess implements TerminProcess {

    private ZkClientx zookeeper = ZooKeeperClient.getInstance();

    public boolean process(TerminEventData data) {
        return doProcess(data, false);
    }

    private boolean doProcess(TerminEventData data, boolean retry) {
        Long pipelineId = data.getPipelineId();
        Long processId = data.getProcessId();

        List<String> currentStages = null;
        try {
            currentStages = zookeeper.getChildren(StagePathUtils.getProcess(pipelineId, processId));
            Collections.sort(currentStages, new StageComparator());
        } catch (ZkNoNodeException e) {
            // ignore，说明节点已经被删除了
            return false;
        } catch (ZkException e) {
            throw new ArbitrateException("Termin_process", e);
        }

        // 按顺序删除对应的S.E.T.L节点
        // s节点
        if (currentStages == null || currentStages.contains(ArbitrateConstants.NODE_SELECTED)) {
            try {
                boolean successed = zookeeper.delete(StagePathUtils.getSelectStage(pipelineId, processId));
                if (!successed) {
                    processDeleteFailed();
                }
            } catch (ZkException e) {
                throw new ArbitrateException("Termin_process", e);
            }
        }

        // e节点
        if (currentStages == null || currentStages.contains(ArbitrateConstants.NODE_EXTRACTED)) {
            try {
                boolean successed = zookeeper.delete(StagePathUtils.getExtractStage(pipelineId, processId));
                if (!successed) {
                    processDeleteFailed();
                }
            } catch (ZkException e) {
                throw new ArbitrateException("Termin_process", e);
            }
        }

        // t节点
        if (currentStages == null || currentStages.contains(ArbitrateConstants.NODE_TRANSFORMED)) {
            try {
                boolean successed = zookeeper.delete(StagePathUtils.getTransformStage(pipelineId, processId));
                if (!successed) {
                    processDeleteFailed();
                }
            } catch (ZkException e) {
                throw new ArbitrateException("Termin_process", e);
            }
        }
        // l节点
        // try {
        // zookeeper.delete(StagePathUtils.getLoadStage(pipelineId, processId), -1, new VoidCallback() {
        //
        // public void processResult(int rc, String path, Object ctx) {
        // logger.debug("delete {} successful. ", path);
        // }
        // }, null);
        // } catch (NoNodeException e) {
        // // ignore,说明节点已经被删除
        // } catch (KeeperException e) {
        // throw new ArbitrateException("Termin_process", e);
        // } catch (InterruptedException e) {
        // // ignore
        // }

        // 针对transform删除成功，s/e有一个删除失败，这说明normal和rollback/shutdown一定有并发
        // 不过会有遗漏判断，比如并发时都是一个线程全删除成功
        return processDelete(data, CollectionUtils.isEmpty(currentStages), retry);
    }

    private boolean processDelete(TerminEventData data, boolean noStage, boolean retry) {
        Long pipelineId = data.getPipelineId();
        Long processId = data.getProcessId();

        boolean result = false;
        // process节点
        // 最后删除一下process节点
        String path = StagePathUtils.getProcess(pipelineId, processId);
        byte[] bytes = null;
        try {
            bytes = zookeeper.readData(path);
        } catch (ZkNoNodeException e) {
            return false;// 说明节点已经被删除了，直接忽略
        }

        ProcessNodeEventData nodeData = JsonUtils.unmarshalFromByte(bytes, ProcessNodeEventData.class);
        if (nodeData.getStatus().isUsed()) {// 如果已使用在标记为true，需要创建termin节点
            // 只删除已经被使用了的节点

            if (noStage && nodeData.getMode().isZookeeper()) {// 针对rpc mode就是没有stage，不需要进行sleep
                // 处理一种case:
                // 针对两个并发操作，一个已经完成了s/e/t/l模块的所有delete，另一个刚好进来发现没有可delete的
                // 这时两个线程就一起进入createTermin操作，存在一些并发问题，针对这种case，需要错开一下
                // 不过这种情况可能会有误判，针对s模块没有处理完成，发起了一次rollback/shutdown操作就会碰上，概率比较小，忽略这种误判吧
                processDeleteFailed();
                return processDelete(data, false, retry);// 再重新尝试访问一下process，看下是否已经被删除了
            }

            // at 2012-09-06，备注一下一个问题排查的结果
            // deleteFailed值判断存在问题，针对非正常结束的process，可能就是没有s/e/t/l节点，就会出现一次sleep+retry操作
            // 在这段sleep的过程中，process可能还会跑一段，产生新的s/e/t节点，导致process删除失败，从而重复执行了createTermin
            if (!retry) {
                // modify at 2012-08-14 , 遇到一个并发bug
                // 1. 两个process a和b，a先执行完毕删除了process节点，b立马得到触发并在极端的时间内处理完成
                // 2. 最后的一个结果b创建的termin要早于a创建的termin，导致termin发送顺序不对
                // 这里修改为，先创建termin节点，再删除对应的process，触发下一个process，保证termin创建为顺序
                // 同样可以避免删除了process后，termin信号创建失败的问题

                // modify at 2012-09-06 , 遇到一个并发bug
                // 一个process只完成了s/e模块，然后进行shutdown操作，完成了termin节点创建，但在process delete时，老的process创建了t节点
                // 这时会出现process删除失败，从而触发进行一次retry操作，此时retry又会再一次创建了termin信号，导致调度出错
                // 所以这里做了一个控制，只有针对非retry模式下才会创建termin信号
                result = createTermin(data, pipelineId, processId);// 创建termin节点
            }
        }

        try {
            // 修改为false，已经有另一个线程添加了该节点
            result = zookeeper.deleteRecursive(StagePathUtils.getProcess(pipelineId, processId));
            if (!result) {
                doProcess(data, true);// 做一次重试，可能做manager关闭的时侯，node节点还跑了一段，导致stage节点又创建了一个
            }
        } catch (ZkInterruptedException e) {
            throw e;
        } catch (ZkException e) {
            doProcess(data, true);// 做一次重试，可能做manager关闭的时侯，node节点还跑了一段，导致stage节点又创建了一个
        }

        return result;
    }

    private boolean createTermin(TerminEventData data, Long pipelineId, Long processId) {
        // 1. 创建end节点
        String path = StagePathUtils.getTermin(pipelineId, processId);
        data.setCurrNid(ArbitrateConfigUtils.getCurrentNid());
        // 序列化
        byte[] bytes = JsonUtils.marshalToByte(data);
        try {
            zookeeper.create(path, bytes, CreateMode.PERSISTENT);
        } catch (ZkNodeExistsException e) {
            // ignore
            return false;
        } catch (ZkException e) {
            throw new ArbitrateException("Termin_single", e);
        }

        return true;
    }

    private void processDeleteFailed() {
        try {
            // 出现并发删除，其中某个人需要放慢一下步骤，等另一个人完成
            Thread.sleep(500 + RandomUtils.nextInt(500));
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
