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

import java.util.List;

public interface OtterControllerMBean {

    /**
     * 返回当前运行pipeline数量，可能只运行S/E/T/L的某个模块
     */
    public int getRunningPipelineCount();

    /**
     * 返回当前运行中的pipeline的id列表
     */
    public List<Long> getRunningPipelines();

    /**
     * 获取当前使用的heap区大小
     */
    public String getHeapMemoryUsage();

    /**
     * 获取node共享线程线程池的线程数
     */
    public int getThreadPoolSize();

    /**
     * 获取当前node共享线程的当前活跃线程数
     */
    public int getThreadActiveSize();

    /**
     * 获取系统对应的load
     */
    public String getNodeSystemInfo();

    /**
     * 获取node节点对应的版本信息
     */
    public String getNodeVersionInfo();

    /**
     * 当前节点是否运行select
     */
    public boolean isSelectRunning(Long pipelineId);

    /**
     * 当前节点是否运行extract
     */
    public boolean isExtractRunning(Long pipelineId);

    /**
     * 当前节点是否运行transform
     */
    public boolean isTransformRunning(Long pipelineId);

    /**
     * 当前节点是否运行load
     */
    public boolean isLoadRunning(Long pipelineId);

    /**
     * 设置是否开启profile统计
     */
    public void setProfile(boolean profile);

    /**
     * 设置对应的s/e/t/l seda模型的线程池大小
     */
    public void setThreadPoolSize(int size);

    // ============ 运行信息 =========

    /**
     * select stage统计信息
     */
    public String selectStageAggregation(Long pipelineId);

    /**
     * extract stage统计信息
     */
    public String extractStageAggregation(Long pipelineId);

    /**
     * transform stage统计信息
     */
    public String transformStageAggregation(Long pipelineId);

    /**
     * load stage统计信息
     */
    public String loadStageAggregation(Long pipelineId);

    /**
     * select pending队列信息
     */
    public String selectPendingProcess(Long pipelineId);

    /**
     * extract pending队列信息
     */
    public String extractPendingProcess(Long pipelineId);

    /**
     * transform pending队列信息
     */
    public String transformPendingProcess(Long pipelineId);

    /**
     * load pending队列信息
     */
    public String loadPendingProcess(Long pipelineId);
}
