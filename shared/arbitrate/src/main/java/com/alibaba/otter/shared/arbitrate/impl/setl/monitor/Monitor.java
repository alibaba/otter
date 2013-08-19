package com.alibaba.otter.shared.arbitrate.impl.setl.monitor;

/**
 * Arbitrate Monitor的统一接口定义，允许进行数据的reload<br/>
 * 在并发往zookeeper写数据，通过Watcher进行监听时，Watcher响应到重新注册Watcher这段时间的数据不能得到响应， 所以需要定时进行reload，避免死锁
 * 
 * @author jianghang 2011-9-19 下午02:51:36
 * @version 4.0.0
 */
public interface Monitor {

    public void reload();

}
