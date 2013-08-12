package com.alibaba.otter.shared.arbitrate.impl.zookeeper;


/**
 * zookeeper 出现session expired异常后的通知，允许业务实现自处理，比如重建ephemeral对象
 * 
 * @author jianghang 2012-1-13 上午10:46:34
 * @version 4.0.0
 */
public interface SessionExpiredNotification {

    public void notification();
}
