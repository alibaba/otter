package com.alibaba.otter.shared.communication.core;

import com.alibaba.otter.shared.communication.core.model.Callback;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 通讯服务类
 * 
 * @author jianghang 2011-9-9 下午04:12:38
 */
public interface CommunicationClient {

    /**
     * 启动communication客户端
     */
    public void initial();

    /**
     * 关闭communication客户端
     */
    public void destory();

    /**
     * 指定对应的地址，进行event调用. 地址格式为：127.0.0.1:1099
     * 
     * @param nid
     * @param event
     */
    public Object call(final String addr, final Event event);

    /**
     * 指定对应的地址，进行event调用，并注册对应的callback接口. 地址格式为：127.0.0.1:1099
     * 
     * <pre>
     * 注意：该方法为异步调用
     * </pre>
     * 
     * @param nid
     * @param event
     */
    public void call(final String addr, Event event, final Callback callback);

    /**
     * 指定对应的地址列表，进行event调用. 地址格式为：127.0.0.1:1099
     * 
     * @param nid
     * @param event
     */
    public Object call(final String[] addrs, final Event event);

    /**
     * 指定对应的地址列表，进行event调用，并注册对应的callback接口. 地址格式为：127.0.0.1:1099
     * 
     * <pre>
     * 注意：该方法为异步调用
     * </pre>
     * 
     * @param nid
     * @param event
     */
    public void call(final String[] serveraddrs, final Event event, final Callback callback);

}
