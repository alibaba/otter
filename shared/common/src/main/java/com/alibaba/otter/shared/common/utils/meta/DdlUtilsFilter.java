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

package com.alibaba.otter.shared.common.utils.meta;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author zebin.xuzb @ 2012-8-8
 * @version 4.1.0
 */
public abstract class DdlUtilsFilter {

    /**
     * 返回要获取 {@linkplain DatabaseMetaData} 的 {@linkplain Connection}，不能返回null
     * 
     * @param con
     * @return
     */
    public Connection filterConnection(Connection con) throws Exception {
        return con;
    }

    /**
     * 对 databaseMetaData 做一些过滤,返回 {@linkplain DatabaseMetaData}，不能为 null
     * 
     * @param databaseMetaData
     * @return
     */
    public DatabaseMetaData filterDataBaseMetaData(JdbcTemplate jdbcTemplate, Connection con,
                                                   DatabaseMetaData databaseMetaData) throws Exception {
        return databaseMetaData;
    }

}
