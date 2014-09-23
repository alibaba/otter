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

package com.alibaba.otter.shared.arbitrate.impl.manage;

import java.util.ArrayList;
import java.util.List;

import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.zookeeper.CreateMode;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateConstants;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.helper.ManagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * 机器node节点的相关信号
 * 
 * @author jianghang 2011-8-31 下午07:26:02
 */
public class NodeArbitrateEvent implements ArbitrateEvent {

    private ZkClientx zookeeper = ZooKeeperClient.getInstance();

    /**
     * 创建相应的node节点，说明：node节点的生命周期为EPHEMERAL
     * 
     * <pre>
     * 1. 是个同步调用
     * </pre>
     */
    public void init(Long nid) {
        String path = ManagePathUtils.getNode(nid);

        try {
            zookeeper.create(path, new byte[0], CreateMode.EPHEMERAL);// 创建为临时节点
        } catch (ZkException e) {
            throw new ArbitrateException("Node_init", nid.toString(), e);
        }
    }

    /**
     * 销毁的node节点
     * 
     * <pre>
     * 1. 是个同步调用
     * </pre>
     */
    public void destory(Long nid) {
        String path = ManagePathUtils.getNode(nid);

        try {
            zookeeper.delete(path); // 删除节点，不关心版本
        } catch (ZkNoNodeException e) {
            // 如果节点已经不存在，则不抛异常
            // ignore
        } catch (ZkException e) {
            throw new ArbitrateException("Node_destory", nid.toString(), e);
        }
    }

    /**
     * 获取当前存活的节点列表
     */
    public List<Long> liveNodes() {
        String path = ArbitrateConstants.NODE_NID_ROOT;
        try {
            List<String> nids = zookeeper.getChildren(path);
            List<Long> result = new ArrayList<Long>();
            for (String nid : nids) {
                result.add(Long.valueOf(nid));
            }

            return result;
        } catch (ZkException e) {
            throw new ArbitrateException("liveNodes", e);
        }
    }

}
