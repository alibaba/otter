package com.alibaba.otter.manager.biz.monitor;

import java.util.List;

import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;

/**
 * 被动监控者，由其他人喂给他需要监控的数据
 * 
 * @author zebin.xuzb @ 2012-8-30
 * @version 4.1.0
 */
public interface PassiveMonitor {

    public void feed(Object data, Long pipelineId);

    public void feed(Object data, List<AlarmRule> rules);
}
