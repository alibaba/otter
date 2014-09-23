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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.SessionExpiredNotification;

/**
 * node节点重建
 * 
 * @author jianghang 2012-1-13 上午11:16:59
 * @version 4.0.0
 */
public class NodeSessionExpired implements SessionExpiredNotification {

    private static final Logger logger = LoggerFactory.getLogger(NodeSessionExpired.class);
    private NodeArbitrateEvent  nodeEvent;

    public void notification() {
        try {
            nodeEvent.init(ArbitrateConfigUtils.getCurrentNid());
        } catch (Exception e) {
            logger.error("after session expired , init node failed. ", e);
        }
    }

    public void setNodeEvent(NodeArbitrateEvent nodeEvent) {
        this.nodeEvent = nodeEvent;
    }

}
