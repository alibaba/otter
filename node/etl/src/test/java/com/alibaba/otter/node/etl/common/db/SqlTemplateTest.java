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
        sql1 = sqlTemplate.getMergeSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns, null);
        sql2 = sqlTemplate.getMergeSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns, null);
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
        sql1 = sqlTemplate.getMergeSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns, null);
        sql2 = sqlTemplate.getMergeSql(SCHEMA_NAME, TABLE_NAME, pkColumns, columns, null);
        want.bool(sql1 == sql2);
    }

}
