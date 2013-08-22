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

package com.alibaba.otter.node.etl.load.loader.db.interceptor.operation;

/**
 * 基于erosa的日志记录
 * 
 * @author jianghang 2011-10-31 下午02:48:22
 * @version 4.0.0
 */
public class CanalMysqlInterceptor extends AbstractOperationInterceptor {

    public static final String mergeofMysqlSql     = "INSERT INTO {0} (id, {1}) VALUES (?, ?) ON DUPLICATE KEY UPDATE {1} = VALUES({1})";

    public static final String mergeofMysqlInfoSql = "INSERT INTO {0} (id, {1}, {2}) VALUES (?, ? ,?) ON DUPLICATE KEY UPDATE {1} = VALUES({1}) , {2} = VALUES({2})";

    public CanalMysqlInterceptor(){
        super(mergeofMysqlSql, mergeofMysqlInfoSql);
    }

}
