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

package com.alibaba.otter.manager.biz.common.arbitrate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.monitor.PassiveMonitor;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.NodeMonitor;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.listener.NodeListener;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.communication.model.arbitrate.NodeAlarmEvent;
import com.google.common.collect.Lists;

/**
 * 默认的死亡节点处理
 * 
 * @author jianghang 2011-9-26 下午10:37:40
 * @version 4.0.0
 */
public class DeadNodeListener implements NodeListener, InitializingBean, DisposableBean {

    private static final Logger                  logger       = LoggerFactory.getLogger(DeadNodeListener.class);
    private volatile DelayQueue<DeadNodeDelayed> queue        = new DelayQueue<DeadNodeDelayed>();
    private NodeMonitor                          nodeMonitor;
    private ArbitrateManageService               arbitrateManageService;
    private PassiveMonitor                       exceptionRuleMonitor;
    private ChannelService                       channelService;
    private ExecutorService                      executor;
    private long                                 checkTime    = 60 * 1000L;                                     // 30秒
    private volatile List<Long>                  currentNodes = new ArrayList<Long>();                          // 当前存活节点

    public DeadNodeListener(){
    }

    public void afterPropertiesSet() throws Exception {
        nodeMonitor.addListener(this);
        executor = Executors.newFixedThreadPool(1);
        executor.submit(new Runnable() {

            public void run() {
                while (true) {
                    DeadNodeDelayed delay = null;
                    try {
                        delay = queue.take();
                        processDead(delay.getNid());
                    } catch (Throwable e) {
                        logger.error(String.format("error happened with [%s]", delay.toString()), e);
                    }
                }
            }
        });
    }

    public synchronized void processChanged(List<Long> nodes) {
        Set<Long> deadNodes = new HashSet<Long>();
        for (Long node : currentNodes) {
            if (!nodes.contains(node)) {
                deadNodes.add(node);
            }
        }

        currentNodes = nodes;// 切换引用，需设置为volatile保证线程安全&可见性
        // 处理下dead node
        if (deadNodes.size() > 0) {
            for (Long nid : deadNodes) {
                Delayed delayed = new DeadNodeDelayed(nid, checkTime);
                if (!queue.contains(delayed)) {// 不重复添加
                    queue.add(new DeadNodeDelayed(nid, checkTime));
                }
            }
        }
    }

    private void processDead(Long deadNode) {
        List<Long> aliveNodes = nodeMonitor.getAliveNodes(true);
        // 需要考虑一种网络瞬断的情况，会导致node所有出现重连，导致出现restart风暴，执行restart时需要重新check下是否存活
        if (aliveNodes.contains(deadNode)) {
            logger.warn("dead node[{}] happend just one moment , check it's alived", deadNode);
            return;
        }

        // 发送一条报警信息
        List<Long> channelIds = Lists.newArrayList();
        List<Channel> channels = channelService.listByNodeId(deadNode, ChannelStatus.START);
        for (Channel channel : channels) {
            channelIds.add(channel.getId());
        }
        Collections.sort(channelIds);

        NodeAlarmEvent alarm = new NodeAlarmEvent();
        alarm.setPipelineId(-1L);
        alarm.setTitle(MonitorName.EXCEPTION.name());
        alarm.setMessage(String.format("nid:%s is dead and restart cids:%s", String.valueOf(deadNode),
                                       channelIds.toString()));
        try {
            exceptionRuleMonitor.feed(alarm, alarm.getPipelineId());
        } catch (Exception e) {
            logger.error(String.format("ERROR # exceptionRuleMonitor error for %s", alarm.toString()), e);
        }

        for (Long channelId : channelIds) {// 重启一下对应的channel
            boolean result = arbitrateManageService.channelEvent().restart(channelId);
            if (result) {
                channelService.notifyChannel(channelId);// 推送一下配置
            }
        }

    }

    public void destroy() throws Exception {
        nodeMonitor.removeListener(this);
        executor.shutdownNow();
    }

    /**
     * 构建一个基于时间的delay queue，解决一个问题：node出现网络闪段，其jvm并不是真实关闭，可以间隔几秒后check一下是否存活再判断是否进行RESTART操作
     * 
     * @author jianghang 2012-8-29 下午01:42:47
     * @version 4.1.0
     */
    public static class DeadNodeDelayed implements Delayed {

        // init time for nano
        private static final long MILL_ORIGIN = System.currentTimeMillis();
        private long              nid;
        private long              now;                                     // 记录具体的now的偏移时间点，单位ms
        private long              timeout;                                 // 记录具体需要被delayed处理的偏移时间点,单位ms

        public DeadNodeDelayed(long nid, long timeout){
            this.nid = nid;
            this.timeout = timeout;
            this.now = System.currentTimeMillis() - MILL_ORIGIN;
        }

        public long getNid() {
            return nid;
        }

        public long getNow() {
            return now;
        }

        public long getDelay(TimeUnit unit) {
            long currNow = System.currentTimeMillis() - MILL_ORIGIN;
            long d = unit.convert(now + timeout - currNow, TimeUnit.MILLISECONDS);
            return d;
        }

        public int compareTo(Delayed other) {
            if (other == this) { // compare zero ONLY if same object
                return 0;
            } else if (other instanceof DeadNodeDelayed) {
                DeadNodeDelayed x = (DeadNodeDelayed) other;
                long diff = now + timeout - (x.now + x.timeout);
                return (diff == 0) ? 0 : ((diff < 0) ? 1 : -1); // 时间越小的，越应该排在前面
            } else {
                long d = (getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS));
                return (d == 0) ? 0 : ((d < 0) ? 1 : -1);
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (nid ^ (nid >>> 32));
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof DeadNodeDelayed)) {
                return false;
            }
            DeadNodeDelayed other = (DeadNodeDelayed) obj;
            if (nid != other.nid) {
                return false;
            }
            return true;
        }

    }

    public void setNodeMonitor(NodeMonitor nodeMonitor) {
        this.nodeMonitor = nodeMonitor;
    }

    public void setArbitrateManageService(ArbitrateManageService arbitrateManageService) {
        this.arbitrateManageService = arbitrateManageService;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public void setExceptionRuleMonitor(PassiveMonitor exceptionRuleMonitor) {
        this.exceptionRuleMonitor = exceptionRuleMonitor;
    }

}
