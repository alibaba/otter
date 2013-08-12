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
