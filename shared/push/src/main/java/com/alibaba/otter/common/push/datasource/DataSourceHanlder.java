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

package com.alibaba.otter.common.push.datasource;

import javax.sql.DataSource;

import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;

/**
 * 针对 {@link DataSourceService} 操作 {@link DataSource} 的扩展机制
 * 
 * @author zebin.xuzb @ 2012-7-26
 * @version 4.1.0
 */
public interface DataSourceHanlder {

    /**
     * Handler 是否支持此配置的 dataSource
     * 
     * @return
     */
    boolean support(DbMediaSource dbMediaSource);

    /**
     * Handler 是否支持此 dataSource
     * 
     * @param dataSource
     * @return
     */
    boolean support(DataSource dataSource);

    /**
     * 扩展功能,可以自定义一些自己实现的 {@link DataSource} <br/>
     * 如果返回的 {@link DataSourceService} 不为空，则 {@link DataSourceService} 会采用此 {@link DataSourceService}
     * 
     * @return
     */
    DataSource create(Long pipelineId, DbMediaSource dbMediaSource);

    /**
     * 扩展功能,可以在 {@link DataSource} 被 destroy 之前做一些事情<br/>
     * 如果返回 <code>true</code>，则暗示此 dataSource 不会被后续流程 destroy. 通常 filter 可以自己 destroy 自己在 preFilter 产生的 dataSource.
     * 
     * @param dataSource
     * @return
     */
    boolean destory(Long pipelineId);

}
