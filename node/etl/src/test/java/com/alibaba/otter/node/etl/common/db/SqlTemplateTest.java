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

package com.alibaba.otter.node.etl.common.db;

import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.BaseDbTest;
import com.alibaba.otter.node.etl.common.db.dialect.SqlTemplate;
import com.alibaba.otter.node.etl.common.db.dialect.mysql.MysqlSqlTemplate;
import com.alibaba.otter.node.etl.common.db.dialect.oracle.OracleSqlTemplate;

public class SqlTemplateTest extends BaseDbTest {

    private static final String SCHEMA_NAME = "srf";
    private static final String TABLE_NAME  = "columns";

    private String[]            pkColumns   = { "id", "name" };
    private String[]            columns     = { "alias_name", "amount", "text_b", "text_c", "curr_date", "gmt_create",
            "gmt_modify"                   };

    @Test
    public void test_mysql() {
        SqlTemplate sqlTemplate = new MysqlSqlTemplate();
        // 执行insert
        String sql1 = sqlTemplate.getInsertSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns);
        String sql2 = sqlTemplate.getInsertSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns);
        want.bool(sql1 == sql2);
        // 执行update
        sql1 = sqlTemplate.getUpdateSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns);
        sql2 = sqlTemplate.getUpdateSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns);
        want.bool(sql1 == sql2);
        // 执行deleate
        sql1 = sqlTemplate.getDeleteSql(SCHEMA_NAME, TABLE_NAME, pkColumns);
        sql2 = sqlTemplate.getDeleteSql(SCHEMA_NAME, TABLE_NAME, pkColumns);
        want.bool(sql1 == sql2);
        // 执行merge
        sql1 = sqlTemplate.getMergeSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns, null, true);
        sql2 = sqlTemplate.getMergeSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns, null, true);
        want.bool(sql1 == sql2);

    }

    @Test
    public void test_oracle() {
        SqlTemplate sqlTemplate = new OracleSqlTemplate();
        // 执行insert
        String sql1 = sqlTemplate.getInsertSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns);
        String sql2 = sqlTemplate.getInsertSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns);
        want.bool(sql1 == sql2);
        // 执行update
        sql1 = sqlTemplate.getUpdateSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns);
        sql2 = sqlTemplate.getUpdateSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns);
        want.bool(sql1 == sql2);
        // 执行deleate
        sql1 = sqlTemplate.getDeleteSql(SCHEMA_NAME, TABLE_NAME, pkColumns);
        sql2 = sqlTemplate.getDeleteSql(SCHEMA_NAME, TABLE_NAME, pkColumns);
        want.bool(sql1 == sql2);
        // 执行merge
        sql1 = sqlTemplate.getMergeSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns, null, true);
        sql2 = sqlTemplate.getMergeSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns, null, true);
        want.bool(sql1 == sql2);
    }

}
