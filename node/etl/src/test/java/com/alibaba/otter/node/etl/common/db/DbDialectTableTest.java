package com.alibaba.otter.node.etl.common.db;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.BaseDbTest;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
import com.alibaba.otter.shared.common.model.config.data.db.DbDataMedia;

/**
 * 测试下table表的获取操作
 * 
 * @author jianghang 2012-4-20 下午04:56:43
 * @version 4.0.2
 */
public class DbDialectTableTest extends BaseDbTest {

    @SpringBeanByName
    private DbDialectFactory dbDialectFactory;

    @Test
    public void testMysqlTable() {
        DbDataMedia mysqlMedia = getMysqlMedia();
        DbDialect dbDialect = dbDialectFactory.getDbDialect(1L, mysqlMedia.getSource());
        Table table = dbDialect.findTable(mysqlMedia.getNamespace(), mysqlMedia.getName());
        want.object(table).notNull();

        System.out.println("tableName = " + table.getName());
        Column[] columns = table.getColumns();
        for (Column column : columns) {
            System.out.println("columnName = " + column.getName() + ",columnType = " + column.getTypeCode()
                               + ",isPrimary = " + column.isPrimaryKey() + ",nullable = " + column.isRequired());
        }

    }

    @Test
    public void testOracleTable() {
        DbDataMedia oracleMedia = getOracleMedia();
        DbDialect dbDialect = dbDialectFactory.getDbDialect(1L, oracleMedia.getSource());
        Table table = dbDialect.findTable(oracleMedia.getNamespace(), oracleMedia.getName());
        want.object(table).notNull();

        System.out.println("tableName = " + table.getName());
        Column[] columns = table.getColumns();
        for (Column column : columns) {
            System.out.println("columnName = " + column.getName() + ",columnType = " + column.getTypeCode()
                               + ",isPrimary = " + column.isPrimaryKey() + ",nullable = " + column.isRequired());
        }
    }
}
