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

package com.alibaba.otter.node.etl.common.datasource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 类TestMysqlUnsignedInt.java的实现描述
 * 
 * @author xiaoqing.zhouxq 2011-12-23 上午10:03:37
 */
public class TestMysqlUnsignedInt {

    public static void insertNumeric() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Properties from = new Properties();
        from.put("user", "root");
        from.put("password", "root");
        from.put("characterEncoding", "utf8");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/erosa", from);
        PreparedStatement pst = conn.prepareStatement("insert into unsignednumeric(id,id1,id2,id3) values (?,?,?,?)");
        pst.setLong(1, Integer.MAX_VALUE * 2L);
        pst.setLong(2, Integer.MAX_VALUE);
        pst.setBigDecimal(3, new BigDecimal("18446744073709551614"));
        pst.setBigDecimal(4, new BigDecimal("9223372036854775807"));
        pst.executeUpdate();

        pst.close();
        conn.close();
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        insertNumeric();

        Thread.sleep(1000L);

        Class.forName("com.mysql.jdbc.Driver");
        Properties from = new Properties();
        from.put("user", "root");
        from.put("password", "root");
        from.put("characterEncoding", "utf8");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/erosa", from);
        PreparedStatement pst = conn.prepareStatement("select id,id1,id2,id3 from unsignednumeric");
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            //            try {
            //                System.out.println(rs.getInt(1));
            //            } catch (Exception e) {
            //                System.out.println(rs.getLong(1));
            //            }
            System.out.println(rs.getLong(1));
            System.out.println(rs.getLong(2));
            System.out.println(rs.getBigDecimal(3));
            //            System.out.println(rs.getString(3));
            System.out.println(rs.getBigDecimal(4));
            System.out.println("-----------------------------");
        }

        rs.close();
        pst.close();
        conn.close();
    }

}
