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

package com.alibaba.otter.node.etl.common.db.dialect;

import org.apache.ddlutils.model.Table;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 数据库方言定义接口
 * 
 * @author jianghang 2011-10-27 上午11:24:15
 * @version 4.0.0
 */
public interface DbDialect {

    public String getName();

    public String getVersion();

    public int getMajorVersion();

    public int getMinorVersion();

    public String getDefaultSchema();

    public String getDefaultCatalog();

    public boolean isCharSpacePadded();

    public boolean isCharSpaceTrimmed();

    public boolean isEmptyStringNulled();

    public boolean isSupportMergeSql();

    public boolean isDRDS();

    public LobHandler getLobHandler();

    public JdbcTemplate getJdbcTemplate();

    public TransactionTemplate getTransactionTemplate();

    public SqlTemplate getSqlTemplate();

    public Table findTable(String schema, String table);

    public Table findTable(String schema, String table, boolean useCache);

    public String getShardColumns(String schema, String table);

    public void reloadTable(String schema, String table);

    public void destory();
}
