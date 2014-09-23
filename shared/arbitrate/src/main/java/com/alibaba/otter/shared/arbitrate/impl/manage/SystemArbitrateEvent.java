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

import org.I0Itec.zkclient.exception.ZkBadVersionException;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.alibaba.otter.shared.arbitrate.impl.ArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.helper.ManagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.zookeeper.ZkClientx;

/**
 * otter系统节点初始化
 * 
 * @author jianghang 2012-2-16 下午04:38:33
 * @version 4.0.0
 */
public class SystemArbitrateEvent implements ArbitrateEvent {

    private static final Logger logger    = LoggerFactory.getLogger(SystemArbitrateEvent.class);
    private ZkClientx           zookeeper = ZooKeeperClient.getInstance();

    /**
     * 初始化对应的系统节点,同步调用
     */
    public void init() {
        String rootPath = ManagePathUtils.getRoot();
        String channelRootPath = ManagePathUtils.getChannelRoot();
        String nodeRootPath = ManagePathUtils.getNodeRoot();
        try {
            zookeeper.create(rootPath, new byte[0], CreateMode.PERSISTENT);
            zookeeper.create(channelRootPath, new byte[0], CreateMode.PERSISTENT);
            zookeeper.create(nodeRootPath, new byte[0], CreateMode.PERSISTENT);
        } catch (ZkNodeExistsException e) {
            // 如果节点已经存在，则不抛异常
            // ignore
        } catch (ZkException e) {
            throw new ArbitrateException("system_init", e);
        }
    }

    /**
     * 销毁对应的系统节点,同步调用
     */
    public void destory() {
        String rootPath = ManagePathUtils.getRoot();
        String channelRootPath = ManagePathUtils.getChannelRoot();
        String nodeRootPath = ManagePathUtils.getNodeRoot();
        try {
            zookeeper.deleteRecursive(channelRootPath); // 删除节点，不关心版本
            zookeeper.deleteRecursive(nodeRootPath); // 删除节点，不关心版本
            zookeeper.deleteRecursive(rootPath); // 删除节点，不关心版本
        } catch (ZkNoNodeException e) {
            // 如果节点已经不存在，则不抛异常
            // ignore
        } catch (ZkException e) {
            throw new ArbitrateException("system_destory", e);
        }
    }

    /**
     * 手工触发一次主备切换
     */
    public void switchWarmup(Long channelId, Long pipelineId) {
        String path = ManagePathUtils.getMainStem(channelId, pipelineId);
        try {
            while (true) {
                Stat stat = new Stat();
                byte[] bytes = zookeeper.readData(path, stat);
                MainStemEventData mainStemData = JsonUtils.unmarshalFromByte(bytes, MainStemEventData.class);
                mainStemData.setActive(false);
                try {
                    zookeeper.writeData(path, JsonUtils.marshalToByte(mainStemData), stat.getVersion());
                    logger.warn("relase channelId[{}],pipelineId[{}] mainstem successed! ", channelId, pipelineId);
                    break;
                } catch (ZkBadVersionException e) {
                    // ignore , retrying
                }
            }
        } catch (ZkNoNodeException e) {
            // ignore
        } catch (ZkException e) {
            throw new ArbitrateException("releaseMainStem", pipelineId.toString(), e);
        }
    }
}
