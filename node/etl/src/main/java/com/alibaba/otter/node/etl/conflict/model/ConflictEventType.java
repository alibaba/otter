package com.alibaba.otter.node.etl.conflict.model;

import com.alibaba.otter.shared.communication.core.model.EventType;

/**
 * config交互的事件类型
 * 
 * @author jianghang
 */
public enum ConflictEventType implements EventType {

    /** 文件冲突检测 */
    fileConflictDetect;
}
