package com.alibaba.otter.shared.communication.model.arbitrate;

import com.alibaba.otter.shared.communication.core.model.EventType;

public enum ArbitrateEventType implements EventType {

    /** 通知manager关闭 */
    stopChannel,
    /** 报警信息 */
    nodeAlarm,
    /** 通知manager node需要关闭 */
    stopNode,
    /** stage调度通知 */
    stageSingle,
    /** fast stage调度通知 */
    fastStageSingle;
}
