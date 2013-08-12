package com.alibaba.otter.manager.biz.monitor;

import java.util.List;

import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;

/**
 * 主动监控者，需要自己去寻找数据来进行监控
 * 
 * @author zebin.xuzb @ 2012-8-23
 * @version 4.1.0
 */
public interface Monitor {

    public void explore();

    public void explore(Long... pipelineIds);

    public void explore(List<AlarmRule> rules);
}
