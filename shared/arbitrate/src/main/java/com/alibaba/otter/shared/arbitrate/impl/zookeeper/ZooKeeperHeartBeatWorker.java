package com.alibaba.otter.shared.arbitrate.impl.zookeeper;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;

/**
 * zookeeper的心跳机制，保证和zookeeper的连接有请求
 * 
 * <pre>
 * 1. 尽量避免SessionExpired异常
 * 2. 出现SessionExpired/ConnectLoss异常时，能触发重连和链接恢复
 * 
 * <pre>
 * @author jianghang 2012-8-28 下午10:30:33
 * @version 4.1.0
 */
public class ZooKeeperHeartBeatWorker implements InitializingBean, DisposableBean {

    private static final Logger         logger    = LoggerFactory.getLogger(ZooKeeperHeartBeatWorker.class);
    private int                         period    = 3000;
    private ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(
                                                                                    1,
                                                                                    new NamedThreadFactory(
                                                                                                           "Otter-zookeeper-heartbeat"),
                                                                                    new ThreadPoolExecutor.CallerRunsPolicy());
    private NodeArbitrateEvent          nodeEvent;

    public void start() {
        scheduler.scheduleAtFixedRate(new Runnable() {

            public void run() {
                try {
                    nodeEvent.liveNodes();
                } catch (Throwable e) {
                    logger.warn("zookeeper heartbeat has failed.", e);
                }
            }
        }, period, period, TimeUnit.MILLISECONDS);

    }

    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void destroy() throws Exception {
        scheduler.shutdownNow();
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setNodeEvent(NodeArbitrateEvent nodeEvent) {
        this.nodeEvent = nodeEvent;
    }

}
