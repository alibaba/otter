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

package com.alibaba.otter.shared.etl.extend.processor.support;

import javax.sql.DataSource;

/**
 * 获取数据库
 * 
 * @author jianghang 2014-6-11 下午3:15:57
 * @since 4.2.10
 */
public interface DataSourceFetcher {

    /**
     * 获取 DataSource
     */
    DataSource fetch(Long tableId);

}
