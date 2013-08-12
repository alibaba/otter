package com.alibaba.otter.common.push;

/**
 * @author zebin.xuzb 2012-9-19 下午3:29:16
 * @version 4.1.0
 */
public interface SubscribeCallback {

    void callback(String changedInfo);
}
