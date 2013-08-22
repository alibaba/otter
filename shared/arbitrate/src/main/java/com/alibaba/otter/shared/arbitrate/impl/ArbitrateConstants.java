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

package com.alibaba.otter.shared.arbitrate.impl;

/**
 * 仲裁器相关常量定义
 * 
 * @author jianghang
 */
public interface ArbitrateConstants {

    /**
     * otter的根节点
     */
    public String NODE_OTTER_ROOT         = "/otter";

    /**
     * otter的node机器的根节点
     */
    public String NODE_NID_ROOT           = NODE_OTTER_ROOT + "/node";

    /**
     * otter中node节点的format格式,接受nid做为参数
     */
    public String NODE_NID_FORMAT         = NODE_NID_ROOT + "/{0}";

    /**
     * otter的channel的根节点
     */
    public String NODE_CHANNEL_ROOT       = NODE_OTTER_ROOT + "/channel";

    /**
     * otter中channel节点的format格式,接受channelId做为参数
     */
    public String NODE_CHANNEL_FORMAT     = NODE_CHANNEL_ROOT + "/{0}";

    /**
     * otter中pipeline节点的format格式,接受channelId,pipelineId做为参数
     */
    public String NODE_PIPELINE_FORMAT    = NODE_CHANNEL_FORMAT + "/{1}";

    /**
     * otter的remedy的根节点
     */
    public String NODE_REMEDY_ROOT        = NODE_PIPELINE_FORMAT + "/remedy";

    /**
     * otter的process的根节点
     */
    public String NODE_PROCESS_ROOT       = NODE_PIPELINE_FORMAT + "/process";

    /**
     * otter中process节点的format格式,接受channelId,pipelineId,processId做为参数
     */
    public String NODE_PROCESS_FORMAT     = NODE_PROCESS_ROOT + "/{2}";

    /**
     * otter的termin信号的根节点
     */
    public String NODE_TERMIN_ROOT        = NODE_PIPELINE_FORMAT + "/termin";

    /**
     * otter中termin节点的format格式,接受channelId,pipelineId,processId做为参数
     */
    public String NODE_TERMIN_FORMAT      = NODE_TERMIN_ROOT + "/{2}";

    /**
     * otter的lock根节点
     */
    public String NODE_LOCK_ROOT          = NODE_PIPELINE_FORMAT + "/lock";

    /**
     * otter的load的lock节点
     */
    public String NODE_LOCK_LOAD          = "load";

    /**
     * 主导线程的状态节点,为pipeline的子节点
     */
    public String NODE_MAINSTEM           = "mainstem";

    /**
     * select完成状态的节点,为process的子节点
     */
    public String NODE_SELECTED           = "selected";

    /**
     * extract完成状态的节点,为process的子节点
     */
    public String NODE_EXTRACTED          = "extracted";

    /**
     * transform完成状态的节点,为process的子节点
     */
    public String NODE_TRANSFORMED        = "transformed";

    /**
     * load完成状态的节点,为process的子节点
     */
    public String NODE_LOADED             = "loaded";

    /**
     * 在logback的配置文件中定义好的按照各个pipeline进行日志文件输出的键值.
     */
    public String splitPipelineLogFileKey = "otter";
}
