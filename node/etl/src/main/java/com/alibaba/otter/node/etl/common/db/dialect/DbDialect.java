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

    public LobHandler getLobHandler();

    public JdbcTemplate getJdbcTemplate();

    public TransactionTemplate getTransactionTemplate();

    public SqlTemplate getSqlTemplate();

    public Table findTable(String schema, String table);

    public Table findTable(String schema, String table, boolean useCache);

    public void reloadTable(String schema, String table);

    public void destory();
}
