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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.RemedyIndexComparator;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.arbitrate.model.RemedyIndexEventData;
import com.alibaba.otter.shared.arbitrate.model.SyncStatusEventData;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 为setl提供辅助工具的event事件
 * 
 * @author jianghang 2011-10-18 上午10:17:18
 * @version 4.0.0
 */
public class ToolArbitrateEvent implements ArbitrateEvent {

    private static final Logger logger    = LoggerFactory.getLogger(ToolArbitrateEvent.class);
    private ZkClientx           zookeeper = ZooKeeperClient.getInstance();

    /**
     * 阻塞等待授权通过
     * 
     * @param pipelineId
     * @throws InterruptedException
     */
    public void waitForPermit(Long pipelineId) throws InterruptedException {
        Assert.notNull(pipelineId);

        PermitMonitor permitMonitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        permitMonitor.waitForPermit();// 阻塞等待授权
    }

    /**
     * 提供数据接口获取对应pipeline上的状态
     */
    public SyncStatusEventData fetch(Long pipelineId) {
        String path = StagePathUtils.getPipeline(pipelineId);
        try {
            byte[] bytes = zookeeper.readData(path);
            if (bytes == null || bytes.length == 0) {
                SyncStatusEventData evnetData = new SyncStatusEventData();
                evnetData.setPipelineId(pipelineId);
                return evnetData;
            } else {
                return JsonUtils.unmarshalFromByte(bytes, SyncStatusEventData.class);
            }
        } catch (ZkException e) {
            // 没有节点返回空
            throw new ArbitrateException("fetch_SyncStatus", pipelineId.toString(), e);
        }
    }

    /**
     * 提供数据接口更新对应的pipeline上的状态
     */
    public void single(SyncStatusEventData syncStatus) {
        String path = StagePathUtils.getPipeline(syncStatus.getPipelineId());
        try {
            byte[] bytes = JsonUtils.marshalToByte(syncStatus);
            zookeeper.writeData(path, bytes);
            logger.info("## single status : " + syncStatus);
        } catch (ZkException e) {
            throw new ArbitrateException("single_SyncStatus", syncStatus.getPipelineId().toString(), e);
        }
    }

    /**
     * 添加一个index节点
     */
    public void addRemedyIndex(RemedyIndexEventData data) {
        String path = StagePathUtils.getRemedyRoot(data.getPipelineId());
        try {
            zookeeper.create(path + "/" + RemedyIndexEventData.formatNodeName(data), new byte[] {},
                             CreateMode.PERSISTENT);
        } catch (ZkNodeExistsException e) {
            // ignore
        } catch (ZkException e) {
            throw new ArbitrateException("addRemedyIndex", data.getPipelineId().toString(), e);
        }
    }

    /**
     * 删除一个index节点
     */
    public void removeRemedyIndex(RemedyIndexEventData data) {
        String path = StagePathUtils.getRemedyRoot(data.getPipelineId());
        try {
            zookeeper.delete(path + "/" + RemedyIndexEventData.formatNodeName(data));
        } catch (ZkNoNodeException e) {
            // ignore
        } catch (ZkException e) {
            throw new ArbitrateException("removeRemedyIndex", data.getPipelineId().toString(), e);
        }
    }

    /**
     * 查询当前的remedy index记录
     */
    public List<RemedyIndexEventData> listRemedyIndexs(Long pipelineId) {
        String path = StagePathUtils.getRemedyRoot(pipelineId);
        List<RemedyIndexEventData> datas = new ArrayList<RemedyIndexEventData>();
        try {
            List<String> nodes = zookeeper.getChildren(path);
            for (String node : nodes) {
                RemedyIndexEventData data = RemedyIndexEventData.parseNodeName(node);
                data.setPipelineId(pipelineId);
                datas.add(data);
            }
        } catch (ZkException e) {
            throw new ArbitrateException("listRemedyIndexs", pipelineId.toString(), e);
        }

        Collections.sort(datas, new RemedyIndexComparator()); // 做一下排序
        return datas;
    }

    /**
     * 释放对应的pipeline资源，是个同步调用
     */
    public void release(Long pipelineId) {
        ArbitrateFactory.destory(pipelineId);
    }

    public void release() {
        ArbitrateFactory.destory();
    }
}
