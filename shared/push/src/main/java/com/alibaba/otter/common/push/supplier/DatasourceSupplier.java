package com.alibaba.otter.common.push.supplier;

/**
 * @author zebin.xuzb 2012-9-24 上午11:29:01
 * @version 4.1.0
 */
public interface DatasourceSupplier {

    public void start();

    public void stop();

    public boolean isStart();

    /**
     * 客户端可以主动获取 master 的 {@linkplain AuthenticationInfo}
     * 
     * @return
     */
    DatasourceInfo fetchMaster();

    /**
     * 客户端可以注册数据库连接发生变更的callback
     * 
     * @param callback
     */
    void addSwtichCallback(DatasourceChangeCallback callback);

}
