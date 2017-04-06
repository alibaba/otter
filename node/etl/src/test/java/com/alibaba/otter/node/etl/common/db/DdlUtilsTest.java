package com.alibaba.otter.node.etl.common.db;

import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.common.db.utils.DdlUtils;

/**
 * @author agapple 2017年4月6日 下午3:23:25
 * @since 3.1.9
 */
public class DdlUtilsTest {

    @Test
    public void testCreateTable() {
        String sql = "CREATE TABLE old.`old_table` (`id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,`c_varchar2` varchar(32) DEFAULT NULL,`c_nvarchar2` varchar(32) DEFAULT NULL,`c_char` char(32) DEFAULT NULL,`c_nchar` char(32) DEFAULT NULL,`c_number` decimal(11,2) DEFAULT NULL,`c_float` double DEFAULT NULL,`c_long` longtext,`c_date` datetime DEFAULT NULL,`c_binary_float` decimal(65,8) DEFAULT NULL,`c_binary_double` double DEFAULT NULL,`c_timestamp0` datetime DEFAULT NULL,`c_timestamp3` datetime(3) DEFAULT NULL,`c_timestamp6` datetime(6) DEFAULT NULL,`c_clob` longtext,`c_nclob` longtext,`c_blob` longblob,`c_raw` varbinary(2000) DEFAULT NULL,`gmt_create` datetime NOT NULL,`gmt_modified` datetime NOT NULL,PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 ";
        String result = DdlUtils.convert(sql, "old", "old_table", "new", "new_table");
        System.out.println(result);
    }

    @Test
    public void testAlterTable() {
        String sql = "alter table `old`.old_table add column name varchar(32) DEFAULT NULL";
        String result = DdlUtils.convert(sql, "old", "old_table", "new", "new_table");
        System.out.println(result);
    }

    @Test
    public void testDropTable() {
        String sql = "drop table old.`old_table`";
        String result = DdlUtils.convert(sql, "old", "old_table", "new", "new_table");
        System.out.println(result);
    }

    @Test
    public void testRenameTable() {
        String sql = "rename table old.`old_table` to old2";
        String result = DdlUtils.convert(sql, "old", "old_table", "new", "new_table");
        System.out.println(result);

        sql = "rename table old1 to old.`old_table`";
        result = DdlUtils.convert(sql, "old", "old_table", "new", "new_table");
        System.out.println(result);
    }

    @Test
    public void testCreateIndex() {
        String sql = " create index IDX_testNoPK_Name on old.`old_table` (name)";
        String result = DdlUtils.convert(sql, "old", "old_table", "new", "new_table");
        System.out.println(result);
    }

    @Test
    public void testDropIndex() {
        String sql = " drop index IDX_testNoPK_Name on old.`old_table`";
        String result = DdlUtils.convert(sql, "old", "old_table", "new", "new_table");
        System.out.println(result);
    }

    public static void main(String args[]) {
        DdlUtilsTest tester = new DdlUtilsTest();
        tester.testCreateTable();
        tester.testAlterTable();
        tester.testDropTable();
        tester.testRenameTable();
        tester.testCreateIndex();
        tester.testDropIndex();
    }
}
