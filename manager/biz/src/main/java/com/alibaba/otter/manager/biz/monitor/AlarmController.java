package com.alibaba.otter.manager.biz.monitor;

import java.util.Map;

import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;

/**
 * @author zebin.xuzb
 * @version 4.1.0
 */
public interface AlarmController {

    public Map<String, Object> control(AlarmRule rule, String message, Map<String, Object> data);

}
