package com.alibaba.otter.common.push.supplier;

/**
 * callback 处理的时间请尽量短，如果时间太长，请使用异步
 * 
 * @author zebin.xuzb 2012-9-25 下午12:14:28
 * @version 4.1.0
 */
public interface DatasourceChangeCallback {

    void masterChanged(DatasourceInfo newMaster);

}
