package com.alibaba.otter.node.etl;

import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.model.config.data.db.DbDataMedia;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;

public class BaseDbTest extends BaseOtterTest {

    public DbDataMedia getMysqlMedia() {
        DbMediaSource dbMediaSource = new DbMediaSource();
        dbMediaSource.setId(10L);
        dbMediaSource.setDriver("com.mysql.jdbc.Driver");
        dbMediaSource.setUsername("srf");
        dbMediaSource.setPassword("srf");
        dbMediaSource.setUrl("jdbc:mysql://10.20.153.53:3306/srf");
        dbMediaSource.setEncode("UTF-8");
        dbMediaSource.setType(DataMediaType.MYSQL);

        DbDataMedia dataMedia = new DbDataMedia();
        dataMedia.setSource(dbMediaSource);
        dataMedia.setId(1L);
        dataMedia.setName("columns");
        dataMedia.setNamespace("srf");
        return dataMedia;
    }

    public DbDataMedia getOracleMedia() {
        DbMediaSource dbMediaSource = new DbMediaSource();
        dbMediaSource.setId(11L);
        dbMediaSource.setDriver("oracle.jdbc.OracleDriver");
        dbMediaSource.setUsername("srf");
        dbMediaSource.setPassword("srf");
        dbMediaSource.setUrl("jdbc:oracle:thin:@10.20.153.53:1521:crmgsb");
        dbMediaSource.setEncode("UTF-8");
        dbMediaSource.setType(DataMediaType.ORACLE);

        DbDataMedia dataMedia = new DbDataMedia();
        dataMedia.setSource(dbMediaSource);
        dataMedia.setId(2L);
        dataMedia.setName("columns");
        dataMedia.setNamespace("srf");
        return dataMedia;
    }

}
