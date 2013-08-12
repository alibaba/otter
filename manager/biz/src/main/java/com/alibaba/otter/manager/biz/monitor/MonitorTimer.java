package com.alibaba.otter.manager.biz.monitor;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;

/**
 * @author simon 2012-9-11 下午3:25:14
 * @version 4.1.0
 */
public class MonitorTimer extends ConcurrentHashMap<MonitorName, Date> {

    private static final long serialVersionUID = -2129810461060521223L;

}
