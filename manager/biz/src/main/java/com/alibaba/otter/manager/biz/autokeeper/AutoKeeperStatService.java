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

package com.alibaba.otter.manager.biz.autokeeper;

import java.util.List;

import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperConnectionStat;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperServerStat;

/**
 * zookeeper状态查询接口
 * 
 * @author jianghang 2012-9-21 下午02:42:16
 * @version 4.1.0
 */
public interface AutoKeeperStatService {

    /**
     * 根据serverIp查询对应的统计信息，包括Connection/Watch/Ephemeral等统计信息
     * 
     * @param serverIp
     * @return
     */
    public AutoKeeperServerStat findServerStat(String serverIp);

    /**
     * 根据sessionId查询对应的统计信息，包括详细的Connection/Watch/Ephemeral等统计信息
     * 
     * @param sessionId
     * @return
     */
    public AutoKeeperServerStat findServerStatBySessionId(String sessionId);

    /**
     * 根据sessionId查询对应的connction链接
     * 
     * @param sessionId
     * @return
     */
    public AutoKeeperConnectionStat findConnectionBySessionId(String sessionId);

    /**
     * 根据临时节点路径查询对应的connection统计信息
     * 
     * @param path
     * @return
     */
    public AutoKeeperConnectionStat findConnectionByEphemeralPath(String path);

    /**
     * 根据watcher路径查询对应的connection统计信息
     * 
     * @param path
     * @return
     */
    public List<AutoKeeperConnectionStat> findConnectionByWatcherPath(String path);
}
