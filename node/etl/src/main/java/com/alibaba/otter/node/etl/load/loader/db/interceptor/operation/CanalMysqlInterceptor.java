package com.alibaba.otter.node.etl.load.loader.db.interceptor.operation;

/**
 * 基于erosa的日志记录
 * 
 * @author jianghang 2011-10-31 下午02:48:22
 * @version 4.0.0
 */
public class CanalMysqlInterceptor extends AbstractOperationInterceptor {

    public static final String mergeofMysqlSql     = "INSERT INTO {0} (id, {1}) VALUES (?, ?) ON DUPLICATE KEY UPDATE {1} = VALUES({1})";

    public static final String mergeofMysqlInfoSql = "INSERT INTO {0} (id, {1}, {2}) VALUES (?, ? ,?) ON DUPLICATE KEY UPDATE {1} = VALUES({1}) , {2} = VALUES({2})";

    public CanalMysqlInterceptor(){
        super(mergeofMysqlSql, mergeofMysqlInfoSql);
    }

}
