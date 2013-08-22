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

package com.alibaba.otter.node.common.config.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.common.communication.NodeCommmunicationClient;
import com.alibaba.otter.node.common.config.NodeTaskListener;
import com.alibaba.otter.node.common.config.NodeTaskService;
import com.alibaba.otter.node.common.config.model.NodeTask;
import com.alibaba.otter.node.common.config.model.NodeTask.TaskEvent;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.communication.core.CommunicationRegistry;
import com.alibaba.otter.shared.communication.model.arbitrate.StopNodeEvent;
import com.alibaba.otter.shared.communication.model.config.ConfigEventType;
import com.alibaba.otter.shared.communication.model.config.FindTaskEvent;
import com.alibaba.otter.shared.communication.model.config.NotifyChannelEvent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * task节点对应的任务列表管理器
 * 
 * @author jianghang
 */
public class NodeTaskServiceImpl implements NodeTaskService, InitializingBean {

    private static final Logger         logger    = LoggerFactory.getLogger(NodeTaskService.class);

    private NodeCommmunicationClient    nodeCommmunicationClient;
    private InternalConfigClientService configClientService;
    private List<NodeTask>              allTasks  = Collections.synchronizedList(new ArrayList<NodeTask>());
    private List<NodeTask>              incTasks  = Collections.synchronizedList(new ArrayList<NodeTask>());
    private List<NodeTaskListener>      listeners = Collections.synchronizedList(new ArrayList<NodeTaskListener>());

    public NodeTaskServiceImpl(){
        CommunicationRegistry.regist(ConfigEventType.notifyChannel, this);
    }

    public synchronized List<NodeTask> listAllNodeTasks() {
        return allTasks;
    }

    public void afterPropertiesSet() throws Exception {
        // 初始化时调用manager获取channel任务
        initNodeTask();
        if (notifyListener() == false) {
            throw new RuntimeException("init node task failed.");
        }
    }

    private synchronized List<NodeTask> mergeIncNodeTasks() {
        List<NodeTask> tasks = new ArrayList<NodeTask>(incTasks);
        incTasks.clear(); // 清除inc
        if (logger.isInfoEnabled()) {
            logger.info("##merge all NodeTask {}", printNodeTasks(tasks));
        }
        merge(allTasks, tasks);// inc获取后直接丢到all的队列里
        if (logger.isInfoEnabled()) {
            logger.info("##now all NodeTask {}", printNodeTasks(allTasks));
        }

        return tasks;
    }

    private void initNodeTask() {
        // 从manager下获取一下对应的任务列表
        Node node = configClientService.currentNode();
        FindTaskEvent event = new FindTaskEvent();
        event.setNid(node.getId());
        Object obj = nodeCommmunicationClient.callManager(event);
        if (obj != null) {
            List<Channel> channels = (List<Channel>) obj;
            for (Channel channel : channels) {
                // 排除已经分配过的task
                processNodeTask(channel);
            }
        }
    }

    private void processNodeTask(Channel channel) {
        List<NodeTask> addTasks = parseNodeTask(channel);
        if (logger.isInfoEnabled()) {
            logger.info("##merge channel[{}] inc NodeTask {}", channel.getId(), printNodeTasks(addTasks));
        }

        List<NodeTask> tasks = new ArrayList<NodeTask>(incTasks);
        merge(tasks, addTasks);// 合并数据到incTasks中
        merge(incTasks, retain(tasks, allTasks));// 过滤掉allTasks中已有的相同的stage/event类型，比如已经有CREATE动作，不需要重复给出
        if (logger.isInfoEnabled()) {
            logger.info("##now inc NodeTask {}", printNodeTasks(incTasks));
        }
    }

    private String printNodeTasks(List<NodeTask> tasks) {
        StringBuilder builder = new StringBuilder();
        for (NodeTask task : tasks) {
            builder.append("\n=========================");
            builder.append("pipeline:" + task.getPipeline().getId()).append("\n");
            builder.append("\t").append(task.getStage()).append("\n");
            builder.append("\t").append(task.getEvent()).append("\n");
            builder.append("\t").append("shutdown:").append(task.isShutdown()).append("\n");
        }

        return builder.toString();
    }

    // 解析一下tasks为NodeTask对象
    private List<NodeTask> parseNodeTask(Channel channel) {
        List<NodeTask> tasks = new ArrayList<NodeTask>();
        List<Pipeline> pipelines = channel.getPipelines();
        Long nid = configClientService.currentNode().getId();
        TaskEvent taksEvent = null;
        if (channel.getStatus().isStart()) {
            taksEvent = TaskEvent.CREATE;
        } else if (channel.getStatus().isStop()) {
            taksEvent = TaskEvent.DELETE;
        } else if (channel.getStatus().isPause()) {
            // modify by ljh at 2013-01-31 , pause状态也需要启动setl线程
            // 因为在发布的时候，restart指令不会推送指令，导致setl线程没有启动
            // return tasks;
            taksEvent = TaskEvent.CREATE;
        }
        // 处理当前最新的状态
        for (Pipeline pipeline : pipelines) {
            List<Node> sNodes = pipeline.getSelectNodes();
            for (Node node : sNodes) {
                if (nid.equals(node.getId())) {// 判断是否为当前的nid
                    NodeTask task = new NodeTask();
                    task.setPipeline(pipeline);
                    NodeTask matchTask = getMatchTask(tasks, task);
                    if (matchTask == null) {
                        matchTask = task;
                        tasks.add(task);
                    }

                    matchTask.setPipeline(pipeline);
                    matchTask.getStage().add(StageType.SELECT);
                    matchTask.getEvent().add(taksEvent);
                }
            }

            List<Node> eNodes = pipeline.getExtractNodes();
            for (Node node : eNodes) {
                if (nid.equals(node.getId())) {// 判断是否为当前的nid
                    NodeTask task = new NodeTask();
                    task.setPipeline(pipeline);
                    NodeTask matchTask = getMatchTask(tasks, task);
                    if (matchTask == null) {
                        matchTask = task;
                        tasks.add(task);
                    }

                    matchTask.getStage().add(StageType.EXTRACT);
                    matchTask.getEvent().add(taksEvent);
                }
            }

            List<Node> tlNodes = pipeline.getLoadNodes();
            for (Node node : tlNodes) {
                if (nid.equals(node.getId())) {// 判断是否为当前的nid
                    NodeTask task = new NodeTask();
                    task.setPipeline(pipeline);
                    NodeTask matchTask = getMatchTask(tasks, task);
                    if (matchTask == null) {
                        matchTask = task;
                        tasks.add(task);
                    }
                    matchTask.getStage().add(StageType.TRANSFORM);
                    matchTask.getEvent().add(taksEvent);
                    matchTask.getStage().add(StageType.LOAD);
                    matchTask.getEvent().add(taksEvent);
                }
            }
        }

        List<Long> pipelineIds = Lists.transform(channel.getPipelines(), new Function<Pipeline, Long>() {

            public Long apply(Pipeline input) {
                return input.getId();
            }
        });
        // 合并一下target中特有的记录，取一下反操作，表示要关闭
        for (NodeTask task : allTasks) {
            Pipeline pipeline = task.getPipeline();
            if (pipeline.getChannelId().equals(channel.getId()) && !pipelineIds.contains(pipeline.getId())) {
                // /是同一个channel，但对应的pipeline不在列表里
                // 处理pipeline删除
                NodeTask deletePipelineTask = new NodeTask();
                deletePipelineTask.setPipeline(pipeline);

                List<StageType> stages = task.getStage();
                List<TaskEvent> events = task.getEvent();
                for (int i = 0; i < stages.size(); i++) {
                    StageType stage = stages.get(i);
                    TaskEvent event = events.get(i);
                    if (event.isCreate()) {
                        deletePipelineTask.getStage().add(stage);
                        deletePipelineTask.getEvent().add(TaskEvent.DELETE);// 添加为关闭
                    }
                }

                tasks.add(deletePipelineTask);
            }

            if (pipelineIds.contains(pipeline.getId())) {// 在当前的channel列表中
                boolean needAdd = false;
                NodeTask matchTask = getMatchTask(tasks, task);// 找到对应的匹配
                if (matchTask == null) {
                    matchTask = new NodeTask();
                    matchTask.setPipeline(pipeline);
                    needAdd = true;
                }
                List<StageType> stages = task.getStage();
                List<TaskEvent> events = task.getEvent();
                for (int i = 0; i < stages.size(); i++) {
                    StageType stage = stages.get(i);
                    TaskEvent event = events.get(i);
                    TaskEvent matchEvent = getMatchStage(matchTask, stage);
                    if (matchEvent == null && event.isCreate()) {// 对应的stage已经被移除，触发一个DELETE操作
                        matchTask.getStage().add(stage);
                        matchTask.getEvent().add(TaskEvent.DELETE);
                    }
                }

                if (needAdd && matchTask.getStage().size() > 0) {
                    tasks.add(matchTask);
                }

            }
        }

        // 判断当前的task是否需要全部关闭
        for (NodeTask task : tasks) {
            boolean shutdown = true;
            for (TaskEvent event : task.getEvent()) {// task已为当前最新节点信息
                shutdown &= event.isDelete();
            }
            task.setShutdown(shutdown);
        }
        return tasks;
    }

    private List<NodeTask> retain(List<NodeTask> targetTasks, List<NodeTask> sourceTasks) {
        List<NodeTask> result = new ArrayList<NodeTask>();
        for (NodeTask task : targetTasks) {
            // 找到有对应的交集task
            NodeTask sourceTask = getMatchTask(sourceTasks, task);
            if (sourceTask != null) {
                // 做一下交集排它处理
                NodeTask resultTask = retain(task, sourceTask);
                if (resultTask != null) {
                    result.add(resultTask);
                }
            } else {
                result.add(task);
            }

        }
        return result;
    }

    // 将target的目标除去source中的信息
    private NodeTask retain(NodeTask targetTask, NodeTask sourceTask) {
        List<StageType> stages = targetTask.getStage();
        List<TaskEvent> events = targetTask.getEvent();

        List<StageType> mergeStates = new ArrayList<StageType>();
        List<TaskEvent> mergeEvents = new ArrayList<TaskEvent>();
        // 合并两者的交集的数据
        for (int i = 0; i < stages.size(); i++) {
            StageType stage = stages.get(i);
            TaskEvent event = events.get(i);

            // 找到source节点对应的TaskEvent
            TaskEvent sourceEvent = getMatchStage(sourceTask, stage);
            if (sourceEvent != null && sourceEvent != event) {// 存在相同的stage节点，判断event是否相同，不同则则添加
                mergeStates.add(stage);
                mergeEvents.add(event);
            }
        }

        // 添加targtTask中特有的stage/event
        for (int i = 0; i < stages.size(); i++) {
            StageType stage = stages.get(i);
            TaskEvent event = events.get(i);
            if (getMatchStage(sourceTask, stage) == null) {
                mergeStates.add(stage);
                mergeEvents.add(event);
            }
        }

        if (mergeStates.size() > 0) {
            NodeTask result = new NodeTask();
            result.setPipeline(targetTask.getPipeline());
            result.setEvent(mergeEvents);
            result.setStage(mergeStates);
            result.setShutdown(targetTask.isShutdown());
            return result;
        } else {
            return null;
        }

    }

    // 合并两个task列表
    private void merge(List<NodeTask> targetTasks, List<NodeTask> sourceTasks) {
        for (NodeTask task : sourceTasks) {
            // 找到对应的
            NodeTask targetTask = getMatchTask(targetTasks, task);
            if (targetTask != null) {
                // 针对已存在的进行合并
                merge(targetTask, task);
            } else {
                // 添加新的节点
                targetTasks.add(task);
            }

        }
    }

    // 获取pipelineId匹配的的NodeTask
    private NodeTask getMatchTask(List<NodeTask> tasks, NodeTask match) {
        for (NodeTask task : tasks) {
            if (match.getPipeline().getId().equals(task.getPipeline().getId())) {
                return task;
            }
        }

        return null;
    }

    // 合并两个NodeTask对象
    private void merge(NodeTask target, NodeTask source) {
        List<StageType> stages = target.getStage();
        List<TaskEvent> events = target.getEvent();

        List<StageType> mergeStates = new ArrayList<StageType>();
        List<TaskEvent> mergeEvents = new ArrayList<TaskEvent>();
        // 合并两者的交集的数据
        for (int i = 0; i < stages.size(); i++) {
            StageType stage = stages.get(i);
            TaskEvent event = events.get(i);
            mergeStates.add(stage);
            // 找到source节点对应的TaskEvent，使用最新值
            TaskEvent sourceEvent = getMatchStage(source, stage);
            if (sourceEvent == null) {
                mergeEvents.add(event);
            } else {
                mergeEvents.add(sourceEvent);
            }
        }
        // 添加两者的差集，添加source中特有的节点
        List<StageType> sourceStages = source.getStage();
        List<TaskEvent> sourceEvents = source.getEvent();
        for (int i = 0; i < sourceStages.size(); i++) {
            StageType stage = sourceStages.get(i);
            TaskEvent event = sourceEvents.get(i);
            if (mergeStates.contains(stage)) {
                continue;
            }

            mergeStates.add(stage);
            mergeEvents.add(event);
        }
        // 更新一下数据
        target.setEvent(mergeEvents);
        target.setStage(mergeStates);

        target.setShutdown(source.isShutdown());// 更新下shutdown变量
    }

    // 获取匹配stage的TaskEvent对象
    private TaskEvent getMatchStage(NodeTask nodeTask, StageType stage) {
        List<StageType> stages = nodeTask.getStage();
        List<TaskEvent> events = nodeTask.getEvent();

        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i) == stage) {
                return events.get(i);
            }

        }

        return null;
    }

    // ===================== 事件处理 =====================

    /**
     * 接受manager的channel变更事件
     */
    protected synchronized boolean onNotifyChannel(NotifyChannelEvent event) {
        configClientService.createOrUpdateChannel(event.getChannel()); // 更新本地的config数据
        processNodeTask(event.getChannel());
        return notifyListener();
    }

    private synchronized boolean notifyListener() {
        boolean result = true;
        List<NodeTask> incNodeTask = new ArrayList<NodeTask>(incTasks);
        if (CollectionUtils.isEmpty(listeners) == false) {
            for (NodeTaskListener listener : listeners) {
                result &= listener.process(incNodeTask);
            }

            if (result) {
                mergeIncNodeTasks();
            } else {
                incTasks.clear();// 清除本次的增量数据
                if (logger.isInfoEnabled()) {
                    logger.info("##notify listener error {}", printNodeTasks(incNodeTask));
                }
            }
        }
        return result;
    }

    public void stopNode() {
        Node node = configClientService.currentNode();
        StopNodeEvent event = new StopNodeEvent();
        event.setNid(node.getId());
        nodeCommmunicationClient.callManager(event);
    }

    public void addListener(NodeTaskListener listener) {
        Assert.notNull(listener);
        this.listeners.add(listener);
        notifyListener(); // 触发一次listener推送
    }

    // ===================== setter / getter =========================

    public void setNodeCommmunicationClient(NodeCommmunicationClient nodeCommmunicationClient) {
        this.nodeCommmunicationClient = nodeCommmunicationClient;
    }

    public void setConfigClientService(InternalConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setListeners(List<NodeTaskListener> listeners) {
        this.listeners = listeners;
    }

}
