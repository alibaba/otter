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

package com.alibaba.otter.node.common.config;

import java.util.List;

import com.alibaba.otter.node.common.config.model.NodeTask;

/**
 * Node节点任务分发的服务类
 * 
 * @author jianghang
 */
public interface NodeTaskService {

    /**
     * 根据对应的获取任务列表，<strong>注意是所有的任务</strong>
     */
    public List<NodeTask> listAllNodeTasks();

    /**
     * 注册监听器
     */
    public void addListener(NodeTaskListener listener);

    /**
     * 关闭node
     */
    public void stopNode();

}
