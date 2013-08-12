package com.alibaba.otter.shared.etl.extend.processor;

import com.alibaba.otter.shared.etl.model.EventData;

/**
 * 业务自定义处理过程
 * 
 * @author jianghang 2012-6-25 下午02:26:36
 * @version 4.1.0
 */
public interface EventProcessor {

    /**
     * 自定义处理单条EventData对象
     * 
     * @return {@link EventData} 返回值=null，需要忽略该条数据
     */
    public EventData process(EventData eventData);
}
