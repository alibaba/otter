package com.alibaba.otter.node.etl;

import java.sql.Types;

import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.common.db.utils.SqlUtils;

public class SqlUtilsTest extends BaseOtterTest {

    @Test
    public void testClob() {
        Object result = SqlUtils.stringToSqlValue("", Types.CLOB, true, false);
        want.object(result).isEqualTo("");

        // requied = true
        result = SqlUtils.stringToSqlValue(null, Types.CLOB, true, false);
        want.object(result).isEqualTo(" ");

        result = SqlUtils.stringToSqlValue(null, Types.CLOB, true, true);
        want.object(result).isEqualTo(" ");

        // requied = false
        result = SqlUtils.stringToSqlValue(null, Types.CLOB, false, false);
        want.object(result).isEqualTo(null);

        result = SqlUtils.stringToSqlValue("", Types.CLOB, false, true);
        want.object(result).isEqualTo(null);
    }

    @Test
    public void testBlob() {
        Object result = SqlUtils.stringToSqlValue("", Types.INTEGER, true, false);
        want.object(result).isEqualTo("");

        // requied = true
        result = SqlUtils.stringToSqlValue(null, Types.BLOB, true, false);
        want.object(result).isEqualTo(null);

        result = SqlUtils.stringToSqlValue(null, Types.BLOB, true, true);
        want.object(result).isEqualTo(null);

        // requied = false
        result = SqlUtils.stringToSqlValue(null, Types.BLOB, false, false);
        want.object(result).isEqualTo(null);

        result = SqlUtils.stringToSqlValue("", Types.BLOB, false, true);
        want.object(result).isEqualTo("");
    }
}
