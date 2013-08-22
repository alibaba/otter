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

package com.alibaba.otter.node.etl.common.datasource;

import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;

/**
 * 抽象所有的data source处理service,并且返回DataMedia的meta信息
 * 
 * @author xiaoqing.zhouxq
 */
public interface DataSourceService {

    /**
     * 返回操作数据源的句柄
     * 
     * @param <T>
     * @param dataMediaId
     * @return
     */
    <T> T getDataSource(long pipelineId, DataMediaSource dataMediaSource);

    /**
     * 释放当前pipeline的数据源.
     * 
     * @param pipeline
     */
    void destroy(Long pipelineId);

}
