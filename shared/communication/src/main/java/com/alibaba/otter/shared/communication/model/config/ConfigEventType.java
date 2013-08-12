package com.alibaba.otter.shared.communication.model.config;

import com.alibaba.otter.shared.communication.core.model.EventType;

/**
 * config交互的事件类型
 * 
 * @author jianghang
 */
public enum ConfigEventType implements EventType {

    /** 查询nid对应的任务列表 */
    findTask,
    /** 根据nid查询Node对象 */
    findNode,
    /** 根据id查询对应的channel对象 */
    findChannel,
    /** manager通知task channel的变化 */
    notifyChannel,
    /** 查询media信息 */
    findMedia,
    /** 通知medai信息 */
    notifyMedia;

}
