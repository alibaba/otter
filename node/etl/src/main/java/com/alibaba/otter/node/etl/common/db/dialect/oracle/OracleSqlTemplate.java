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

package com.alibaba.otter.node.etl.common.db.dialect.oracle;

import com.alibaba.otter.node.etl.common.db.dialect.AbstractSqlTemplate;

/**
 * oracle生成模板
 * 
 * @author jianghang 2011-10-27 下午01:41:34
 * @version 4.0.0
 */
public class OracleSqlTemplate extends AbstractSqlTemplate {

    /**
     * http://en.wikipedia.org/wiki/Merge_(SQL)
     */
    public String getMergeSql(String schemaName, String tableName, String[] keyNames, String[] columnNames,
                              String[] viewColumnNames) {
        final String aliasA = "a";
        final String aliasB = "b";
        StringBuilder sql = new StringBuilder();

        sql.append("merge /*+ use_nl(a b)*/ into ").append(getFullName(schemaName, tableName)).append(" ").append(aliasA);
        sql.append(" using (select ");

        int size = columnNames.length;
        // 构建 (select ? as col1, ? as col2 from dual)
        for (int i = 0; i < size; i++) {
            sql.append("? as " + columnNames[i]).append(" , ");
        }
        size = keyNames.length;
        for (int i = 0; i < size; i++) {
            sql.append("? as " + keyNames[i]).append((i + 1 < size) ? " , " : "");
        }
        sql.append(" from dual) ").append(aliasB);
        sql.append(" on (");

        size = keyNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(aliasA + "." + keyNames[i]).append("=").append(aliasB + "." + keyNames[i]);
            sql.append((i + 1 < size) ? " and " : "");
        }

        sql.append(") when matched then update set ");

        size = columnNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(aliasA + "." + columnNames[i]).append("=").append(aliasB + "." + columnNames[i]);
            sql.append((i + 1 < size) ? " , " : "");
        }

        sql.append(" when not matched then insert (");
        size = columnNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(aliasA + "." + columnNames[i]).append(" , ");
        }
        size = keyNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(aliasA + "." + keyNames[i]).append((i + 1 < size) ? " , " : "");
        }

        sql.append(" ) values (");
        size = columnNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(aliasB + "." + columnNames[i]).append(" , ");
        }
        size = keyNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(aliasB + "." + keyNames[i]).append((i + 1 < size) ? " , " : "");
        }
        sql.append(" )");
        return sql.toString().intern(); // intern优化，避免出现大量相同的字符串
    }

}
