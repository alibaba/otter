package com.alibaba.otter.manager.biz.remote;

import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.shared.communication.model.canal.FindCanalEvent;
import com.alibaba.otter.shared.communication.model.canal.FindFilterEvent;

/**
 * canal远程服务接口
 * 
 * @author jianghang 2012-8-1 下午04:12:41
 * @version 4.1.0
 */
public interface CanalRemoteService {

    /**
     * 接收客户端的查询Canal请求
     */
    public Canal onFindCanal(FindCanalEvent event);

    /**
     * 接收客户端的查询filter请求
     */
    public String onFindFilter(FindFilterEvent event);
}
