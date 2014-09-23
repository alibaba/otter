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

package com.alibaba.otter.manager.biz.remote.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.remote.NodeRemoteService;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.google.common.base.Function;
import com.google.common.collect.GenericMapMaker;
import com.google.common.collect.MapEvictionListener;
import com.google.common.collect.MapMaker;

/**
 * 基于node Mbean获取数据的实现
 * 
 * @author jianghang 2012-7-30 上午10:38:30
 */
public class NodeMBeanServiceImpl implements NodeRemoteService {

    private static final String              MBEAN_NAME  = "bean:name=otterControllor";
    private static final String              SERVICE_URL = "service:jmx:rmi://{0}/jndi/rmi://{0}:{1}/mbean";
    private ObjectName                       objectName;
    private NodeService                      nodeService;
    private Map<Long, MBeanServerConnection> mbeanServers;

    public NodeMBeanServiceImpl(){
        try {
            objectName = new ObjectName(MBEAN_NAME);
        } catch (Exception e) {
            throw new ManagerException(e);
        }

        GenericMapMaker mapMaker = null;
        mapMaker = new MapMaker().expireAfterAccess(5, TimeUnit.MINUTES)
            .softValues()
            .evictionListener(new MapEvictionListener<Long, MBeanServerConnection>() {

                public void onEviction(Long nid, MBeanServerConnection mbeanServer) {
                    // do nothing
                }
            });

        mbeanServers = mapMaker.makeComputingMap(new Function<Long, MBeanServerConnection>() {

            public MBeanServerConnection apply(Long nid) {
                Node node = nodeService.findById(nid);
                String ip = node.getIp();
                if (node.getParameters().getUseExternalIp()) {
                    ip = node.getParameters().getExternalIp();
                }

                int port = node.getPort().intValue() + 1;
                Integer mbeanPort = node.getParameters().getMbeanPort();
                if (mbeanPort != null && mbeanPort != 0) {// 做个兼容处理，<=4.2.2版本没有mbeanPort设置
                    port = mbeanPort;
                }

                try {
                    JMXServiceURL serviceURL = new JMXServiceURL(MessageFormat.format(SERVICE_URL,
                        ip,
                        String.valueOf(port)));
                    JMXConnector cntor = JMXConnectorFactory.connect(serviceURL, null);
                    MBeanServerConnection mbsc = cntor.getMBeanServerConnection();
                    return mbsc;
                } catch (Exception e) {
                    throw new ManagerException(e);
                }
            }

        });
    }

    public String getHeapMemoryUsage(Long nid) {
        return (String) getAttribute(nid, "HeapMemoryUsage");
    }

    public String getNodeSystemInfo(Long nid) {
        return (String) getAttribute(nid, "NodeSystemInfo");
    }

    public String getNodeVersionInfo(Long nid) {
        return (String) getAttribute(nid, "NodeVersionInfo");
    }

    public int getRunningPipelineCount(Long nid) {
        return (Integer) getAttribute(nid, "RunningPipelineCount");
    }

    public List<Long> getRunningPipelines(Long nid) {
        return (List<Long>) getAttribute(nid, "RunningPipelines");
    }

    public int getThreadPoolSize(Long nid) {
        return (Integer) getAttribute(nid, "ThreadPoolSize");
    }

    public void setProfile(Long nid, boolean profile) {
        try {
            mbeanServers.get(nid).invoke(objectName,
                "setProfile",
                new Object[] { profile },
                new String[] { "java.lang.Boolean" });
        } catch (Exception e) {
            mbeanServers.remove(nid);
            throw new ManagerException(e);
        }
    }

    public void setThreadPoolSize(Long nid, int size) {
        try {
            mbeanServers.get(nid).invoke(objectName,
                "setThreadPoolSize",
                new Object[] { size },
                new String[] { "java.lang.Integer" });
        } catch (Exception e) {
            mbeanServers.remove(nid);
            throw new ManagerException(e);
        }
    }

    public int getThreadActiveSize(Long nid) {
        return (Integer) getAttribute(nid, "ThreadActiveSize");
    }

    public boolean isSelectRunning(Long nid, Long pipelineId) {
        return (Boolean) invoke(nid, pipelineId, "isSelectRunning");
    }

    public boolean isExtractRunning(Long nid, Long pipelineId) {
        return (Boolean) invoke(nid, pipelineId, "isExtractRunning");
    }

    public boolean isTransformRunning(Long nid, Long pipelineId) {
        return (Boolean) invoke(nid, pipelineId, "isTransformRunning");
    }

    public boolean isLoadRunning(Long nid, Long pipelineId) {
        return (Boolean) invoke(nid, pipelineId, "isLoadRunning");
    }

    public String selectStageAggregation(Long nid, Long pipelineId) {
        return (String) invoke(nid, pipelineId, "selectStageAggregation");
    }

    public String extractStageAggregation(Long nid, Long pipelineId) {
        return (String) invoke(nid, pipelineId, "extractStageAggregation");
    }

    public String transformStageAggregation(Long nid, Long pipelineId) {
        return (String) invoke(nid, pipelineId, "transformStageAggregation");
    }

    public String loadStageAggregation(Long nid, Long pipelineId) {
        return (String) invoke(nid, pipelineId, "loadStageAggregation");
    }

    public String selectPendingProcess(Long nid, Long pipelineId) {
        return (String) invoke(nid, pipelineId, "selectPendingProcess");
    }

    public String extractPendingProcess(Long nid, Long pipelineId) {
        return (String) invoke(nid, pipelineId, "extractPendingProcess");
    }

    public String transformPendingProcess(Long nid, Long pipelineId) {
        return (String) invoke(nid, pipelineId, "transformPendingProcess");
    }

    public String loadPendingProcess(Long nid, Long pipelineId) {
        return (String) invoke(nid, pipelineId, "loadPendingProcess");
    }

    private Object getAttribute(Long nid, String attribute) {
        try {
            return mbeanServers.get(nid).getAttribute(objectName, attribute);
        } catch (Exception e) {
            mbeanServers.remove(nid);
            throw new ManagerException(e);
        }
    }

    private Object invoke(Long nid, Long pipelineId, String method) {
        try {
            return mbeanServers.get(nid).invoke(objectName,
                method,
                new Object[] { pipelineId },
                new String[] { "java.lang.Long" });
        } catch (Exception e) {
            mbeanServers.remove(nid);
            throw new ManagerException(e);
        }
    }

    // ====================== setter / getter ============================

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

}
