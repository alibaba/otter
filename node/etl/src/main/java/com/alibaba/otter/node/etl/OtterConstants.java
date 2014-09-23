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

/**
 * otter 常量定义
 * 
 * @author jianghang 2012-4-21 下午04:20:18
 * @version 4.0.2
 */
public interface OtterConstants {

    public String NID_NAME                      = "nid";

    /**
     * 在logback的配置文件中定义好的按照各个pipeline进行日志文件输出的键值.
     */
    public String splitPipelineLogFileKey       = "otter";

    /**
     * 在logback的配置文件中定义好的按照各个pipeline在load时，归档输出的键值.
     */
    public String splitPipelineLoadLogFileKey   = "load";

    /**
     * 在logback的配置文件中定义好的按照各个pipeline在select时，归档输出的键值.
     */
    public String splitPipelineSelectLogFileKey = "select";
}
