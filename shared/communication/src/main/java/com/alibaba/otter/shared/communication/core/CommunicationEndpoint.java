package com.alibaba.otter.shared.communication.core;

import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 通讯服务端点，需要在每个node上部署后，就可以通过Communication工具进行数据通讯
 * 
 * @author jianghang 2011-9-9 下午04:07:51
 */
public interface CommunicationEndpoint {

    /**
     * 初始化endpint
     */
    public void initial();

    /**
     * 销毁endpoint
     */
    public void destory();

    /**
     * 接受一个消息
     * 
     * @return
     */
    public Object acceptEvent(Event event);

}
