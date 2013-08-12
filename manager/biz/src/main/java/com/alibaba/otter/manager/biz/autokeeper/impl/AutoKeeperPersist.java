package com.alibaba.otter.manager.biz.autokeeper.impl;

/**
 * 数据持久化接口，会有持久化调度器定时触发
 * 
 * @author jianghang 2012-9-21 下午03:04:37
 * @version 4.1.0
 */
public interface AutoKeeperPersist {

    /**
     * 会有
     */
    public void persist();
}
