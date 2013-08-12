package com.alibaba.otter.shared.communication.app.event;

import com.alibaba.otter.shared.communication.core.model.EventType;

/**
 * @author jianghang 2011-9-13 下午08:28:50
 */
public enum AppEventType implements EventType {
    create, update, delete, find;
}
