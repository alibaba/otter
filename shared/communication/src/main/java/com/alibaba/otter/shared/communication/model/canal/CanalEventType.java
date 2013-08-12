package com.alibaba.otter.shared.communication.model.canal;

import com.alibaba.otter.shared.communication.core.model.EventType;

/**
 * config交互的事件类型
 * 
 * @author jianghang
 */
public enum CanalEventType implements EventType {

    /** 查询对应canal信息 */
    findCanal,
    /** 查询对应的过滤条件 */
    findFilter;
}
