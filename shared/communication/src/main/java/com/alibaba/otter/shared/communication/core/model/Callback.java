package com.alibaba.otter.shared.communication.core.model;

/**
 * 通讯的异步callback回调接口
 * 
 * @author jianghang 2011-9-9 下午04:16:04
 */
public interface Callback<PARAM> {

    public void call(PARAM event);
}
