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

package com.alibaba.otter.node.etl.common.io.download.impl.aria2c;

/**
 * @author brave.taoy
 */
public interface Aria2cConfig {

    public static final String[] ARIA2C_PARAM = new String[] {
                                              // 不加载配置文件
            "--no-conf",

            // 每个url下载线程数 1-16
            "-s 16",

            // 最大并发下载数 1-45
            "-j 50",

            // 单机最大连接数
            "-x 16",

            // 每个链接下载的数据大小
            "-k 2M",

            // 连接超时 单位：秒
            "--timeout=600",

            // 最大重试次数
            "--max-tries=5",

            // 停止下载 单位：秒
            "--stop=1800",

            // 覆盖已存在文件
            "--allow-overwrite=true",

            // Set max download speed
            // "--max-overall-download-limit=512K",

            // http长连接
            "--enable-http-keep-alive=true",

            // 日志级别
            "--log-level=warn",              };
}
