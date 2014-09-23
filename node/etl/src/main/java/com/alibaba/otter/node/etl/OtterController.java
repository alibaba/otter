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

package com.alibaba.otter.node.etl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.common.config.NodeTaskListener;
import com.alibaba.otter.node.common.config.NodeTaskService;
import com.alibaba.otter.node.common.config.model.NodeTask;
import com.alibaba.otter.node.common.config.model.NodeTask.TaskEvent;
import com.alibaba.otter.node.etl.common.datasource.DataSourceService;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
import com.alibaba.otter.node.etl.common.jmx.StageAggregationCollector;
import com.alibaba.otter.node.etl.common.task.GlobalTask;
import com.alibaba.otter.node.etl.extract.ExtractTask;
import com.alibaba.otter.node.etl.load.LoadTask;
import com.alibaba.otter.node.etl.select.SelectTask;
import com.alibaba.otter.node.etl.transform.TransformTask;
import com.alibaba.otter.shared.arbitrate.ArbitrateEventService;
import com.alibaba.otter.shared.arbitrate.ArbitrateManageService;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeSessionExpired;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.common.model.config.ConfigException;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.AddressUtils;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.version.VersionInfo;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * 管理和维护对应node机器内的S.E.T.L任务，实时接收manager推送的NodeTask调度信息，可查看 {@linkplain NodeTaskService}
 * 
 * @author jianghang 2012-4-21 下午04:48:12
 * @version 4.0.2
 */
public class OtterController implements NodeTaskListener, OtterControllerMBean {

    private static final Logger                   logger      = LoggerFactory.getLogger(OtterController.class);

    // 第一层为pipelineId，第二层为S.E.T.L模块
    private Map<Long, Map<StageType, GlobalTask>> controllers = new MapMaker().makeComputingMap(new Function<Long, Map<StageType, GlobalTask>>() {

                                                                  public Map<StageType, GlobalTask> apply(Long pipelineId) {
                                                                      return new MapMaker().makeMap();
                                                                  }
                                                              });
    private ConfigClientService                   configClientService;
    private ArbitrateManageService                arbitrateManageService;
    private NodeTaskService                       nodeTaskService;
    // 各种资源管理
    private DataSourceService                     dataSourceService;                                           // 连接池资源
    private DbDialectFactory                      dbDialectFactory;                                            // 数据库信息资源
    private ArbitrateEventService                 arbitrateEventService;                                       // 仲裁器资源
    private ExecutorService                       executorService;

    private StageAggregationCollector             stageAggregationCollector;

    public void start() throws Throwable {
        // 初始化节点
        initNid();
        nodeTaskService.addListener(this); // 将自己添加为NodeTask响应者
    }

    public void stop() throws Throwable {
        for (Map<StageType, GlobalTask> tasks : controllers.values()) {
            for (GlobalTask task : tasks.values()) {
                try {
                    task.shutdown();
                } catch (Exception e) {
                    logger.error("##shutdown task error!", e);
                }
            }
        }

        try {
            Long nid = configClientService.currentNode().getId();
            arbitrateManageService.nodeEvent().destory(Long.valueOf(nid));
        } catch (Exception e) {
            logger.error("##destory node error!", e);
        }

        try {
            arbitrateEventService.toolEvent().release();
        } catch (Exception e) {
            logger.error("##destory arbitrate error!", e);
        }

        try {
            nodeTaskService.stopNode(); // 通知manager停止当前node
        } catch (Exception e) {
            logger.error("##stop node error!", e);
        }

        try {
            OtterContextLocator.close();
        } catch (Exception e) {
            logger.error("##cloes spring error!", e);
        }

        ZooKeeperClient.destory();// 关闭zookeeper
    }

    public boolean process(List<NodeTask> nodeTasks) {
        if (nodeTasks == null || nodeTasks.isEmpty()) {
            return true;
        }

        for (NodeTask nodeTask : nodeTasks) {
            boolean shutdown = nodeTask.isShutdown();
            Long pipelineId = nodeTask.getPipeline().getId();
            if (shutdown) {
                Map<StageType, GlobalTask> tasks = controllers.remove(pipelineId);
                if (tasks != null) {
                    logger.info("INFO ## shutdown this pipeline sync ,the pipelineId = {} and tasks = {}", pipelineId,
                                tasks.keySet());
                    stopPipeline(pipelineId, tasks);
                } else {
                    logger.info("INFO ## this pipeline id = {} is not start sync", pipelineId);
                }
            } else {
                startPipeline(nodeTask);
            }
        }

        return true;
    }

    // ===================== helper method ======================

    public void startPipeline(NodeTask nodeTask) {
        Long pipelineId = nodeTask.getPipeline().getId();
        releasePipeline(pipelineId);
        Map<StageType, GlobalTask> tasks = controllers.get(pipelineId);
        // 处理具体的任务命令
        List<StageType> stage = nodeTask.getStage();
        List<TaskEvent> event = nodeTask.getEvent();
        for (int i = 0; i < stage.size(); i++) {
            StageType stageType = stage.get(i);
            TaskEvent taskEvent = event.get(i);
            if (taskEvent.isCreate()) {
                startTask(nodeTask.getPipeline(), tasks, stageType);
            } else {
                stopTask(tasks, stageType);
            }
        }
    }

    private void startTask(Pipeline pipeline, Map<StageType, GlobalTask> tasks, StageType taskType) {
        if (tasks.get(taskType) != null && tasks.get(taskType).isAlive()) {
            logger.warn("WARN ## this task = {} has started", taskType);
        }

        GlobalTask task = null;
        if (taskType.isSelect()) {
            task = new SelectTask(pipeline.getId());
        } else if (taskType.isExtract()) {
            task = new ExtractTask(pipeline.getId());
        } else if (taskType.isTransform()) {
            task = new TransformTask(pipeline.getId());
        } else if (taskType.isLoad()) {
            task = new LoadTask(pipeline.getId());
        }

        if (task != null) {
            OtterContextLocator.autowire(task); // 注入一下spring资源
            task.start();
            tasks.put(taskType, task);
            logger.info("INFO ## start this task = {} success", taskType.toString());
        }
    }

    private void stopTask(Map<StageType, GlobalTask> tasks, StageType taskType) {
        GlobalTask task = tasks.remove(taskType);
        if (task != null) {
            task.shutdown();
            logger.info("INFO ## taskName = {} has shutdown", taskType);
        } else {
            logger.info("INFo ## taskName = {} is not started", taskType);
        }

    }

    private void stopPipeline(Long pipelineId, Map<StageType, GlobalTask> tasks) {
        for (GlobalTask task : tasks.values()) {
            try {
                task.shutdown();
            } catch (Exception e) {
                logger.error("## stop s/e/t/l task error!", e);
            } finally {
                tasks.remove(task);
            }
        }
        // close other resources.
        try {
            Thread.sleep(1 * 1000); // sleep 5s，等待S.E.T.L释放线程
        } catch (InterruptedException e) {
            logger.error("ERROR ## ", e);
        }

        // 释放资源
        releasePipeline(pipelineId);
        arbitrateEventService.toolEvent().release(pipelineId);
    }

    private void releasePipeline(Long pipelineId) {
        dataSourceService.destroy(pipelineId);
        dbDialectFactory.destory(pipelineId);
    }

    private void initNid() {
        // 获取一下nid变量
        String nid = System.getProperty(OtterConstants.NID_NAME);
        if (StringUtils.isEmpty(nid)) {
            throw new ConfigException("nid is not set!");
        }
        logger.info("INFO ## the nodeId = {}", nid);
        checkNidVaild(nid);
        arbitrateManageService.nodeEvent().init(Long.valueOf(nid));
        // 添加session expired处理
        NodeSessionExpired sessionExpired = new NodeSessionExpired();
        sessionExpired.setNodeEvent(arbitrateManageService.nodeEvent());
        ZooKeeperClient.registerNotification(sessionExpired);
    }

    // 判断本机ip是否和node.getIp()相同
    private void checkNidVaild(String nid) {
        Node node = configClientService.currentNode();
        String hostIp = AddressUtils.getHostIp();
        String nodeIp = node.getIp();
        int nodePort = node.getPort().intValue();
        if (!AddressUtils.isHostIp(nodeIp)) {
            throw new IllegalArgumentException(
                                               String.format("node[%s] ip[%s] port[%s] , but your host ip[%s] is not matched!",
                                                             nid, nodeIp, nodePort, hostIp));
        }
    }

    // ================ mbean info =======================

    public String getHeapMemoryUsage() {
        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        return JsonUtils.marshalToString(memoryUsage);
    }

    public String getNodeSystemInfo() {
        OperatingSystemMXBean mbean = ManagementFactory.getOperatingSystemMXBean();
        StringBuilder buf = new StringBuilder();
        buf.append("").append(mbean.getName()).append(' ').append(mbean.getVersion()).append(' ').append(mbean.getArch());
        buf.append(" @ ").append(mbean.getAvailableProcessors()).append(" cores");
        buf.append(" , 【 load average:").append(mbean.getSystemLoadAverage()).append(" 】");
        return buf.toString();
    }

    public String getNodeVersionInfo() {
        return VersionInfo.getVersion() + " [ r" + VersionInfo.getRevision() + " ] @ " + VersionInfo.getDate();
    }

    public int getRunningPipelineCount() {
        return controllers.size();
    }

    public List<Long> getRunningPipelines() {
        return new ArrayList<Long>(controllers.keySet());
    }

    public int getThreadActiveSize() {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor pool = (ThreadPoolExecutor) executorService;
            return pool.getActiveCount();
        }

        return 0;
    }

    public int getThreadPoolSize() {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor pool = (ThreadPoolExecutor) executorService;
            return pool.getCorePoolSize();
        }

        return 0;
    }

    public void setThreadPoolSize(int size) {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor pool = (ThreadPoolExecutor) executorService;
            pool.setCorePoolSize(size);
            pool.setMaximumPoolSize(size);
        }
    }

    public void setProfile(boolean profile) {
        stageAggregationCollector.setProfiling(profile);
    }

    public boolean isSelectRunning(Long pipelineId) {
        return controllers.get(pipelineId).containsKey(StageType.SELECT);
    }

    public boolean isExtractRunning(Long pipelineId) {
        return controllers.get(pipelineId).containsKey(StageType.EXTRACT);
    }

    public boolean isTransformRunning(Long pipelineId) {
        return controllers.get(pipelineId).containsKey(StageType.TRANSFORM);
    }

    public boolean isLoadRunning(Long pipelineId) {
        return controllers.get(pipelineId).containsKey(StageType.LOAD);
    }

    public String selectStageAggregation(Long pipelineId) {
        return stageAggregationCollector.histogram(pipelineId, StageType.SELECT);
    }

    public String extractStageAggregation(Long pipelineId) {
        return stageAggregationCollector.histogram(pipelineId, StageType.EXTRACT);
    }

    public String transformStageAggregation(Long pipelineId) {
        return stageAggregationCollector.histogram(pipelineId, StageType.TRANSFORM);
    }

    public String loadStageAggregation(Long pipelineId) {
        return stageAggregationCollector.histogram(pipelineId, StageType.LOAD);
    }

    public String selectPendingProcess(Long pipelineId) {
        return pendingProcess(pipelineId, StageType.SELECT);
    }

    public String extractPendingProcess(Long pipelineId) {
        return pendingProcess(pipelineId, StageType.EXTRACT);
    }

    public String transformPendingProcess(Long pipelineId) {
        return pendingProcess(pipelineId, StageType.TRANSFORM);
    }

    public String loadPendingProcess(Long pipelineId) {
        return pendingProcess(pipelineId, StageType.LOAD);
    }

    private String pendingProcess(Long pipelineId, StageType stage) {
        GlobalTask task = controllers.get(pipelineId).get(stage);
        if (task != null) {
            return "stage:" + stage + " , pending:[" + StringUtils.join(task.getPendingProcess(), ',') + "]";
        } else {
            return "node don't running stage:" + stage;
        }
    }

    // ==================== setter / getter =======================

    public void setNodeTaskService(NodeTaskService nodeTaskService) {
        this.nodeTaskService = nodeTaskService;
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setArbitrateManageService(ArbitrateManageService arbitrateManageService) {
        this.arbitrateManageService = arbitrateManageService;
    }

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public void setDbDialectFactory(DbDialectFactory dbDialectFactory) {
        this.dbDialectFactory = dbDialectFactory;
    }

    public void setArbitrateEventService(ArbitrateEventService arbitrateEventService) {
        this.arbitrateEventService = arbitrateEventService;
    }

    public void setStageAggregationCollector(StageAggregationCollector stageAggregationCollector) {
        this.stageAggregationCollector = stageAggregationCollector;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

}
