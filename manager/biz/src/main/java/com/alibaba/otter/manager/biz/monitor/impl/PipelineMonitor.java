package com.alibaba.otter.manager.biz.monitor.impl;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import com.alibaba.otter.manager.biz.monitor.Monitor;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;

/**
 * @author zebin.xuzb @ 2012-8-29
 * @version 4.1.0
 */
public class PipelineMonitor implements Monitor {

    @Resource(name = "delayStatRuleMonitor")
    private Monitor delayStatRuleMonitor;

    // @Resource(name = "exceptionRuleMonitor")
    // private Monitor exceptionRuleMonitor;

    @Resource(name = "pipelineTimeoutRuleMonitor")
    private Monitor pipelineTimeoutRuleMonitor;

    @Resource(name = "processTimeoutRuleMonitor")
    private Monitor processTimeoutRuleMonitor;

    @Resource(name = "pausedRuleMonitor")
    private Monitor pausedRuleMonitor;

    @Resource(name = "positionTimeoutRuleMonitor")
    private Monitor positionTimeoutRuleMonitor;

    // FIXME! 临时实现
    @Override
    public void explore(List<AlarmRule> rules) {
        List<AlarmRule> delayTimeRules = new LinkedList<AlarmRule>();
        List<AlarmRule> exceptonRules = new LinkedList<AlarmRule>();
        List<AlarmRule> pipelineTimeoutRules = new LinkedList<AlarmRule>();
        List<AlarmRule> processTimeoutRules = new LinkedList<AlarmRule>();
        List<AlarmRule> pausedRules = new LinkedList<AlarmRule>();
        List<AlarmRule> positionTimeoutRules = new LinkedList<AlarmRule>();

        Date now = new Date();
        for (AlarmRule rule : rules) {
            switch (rule.getMonitorName()) {
                case DELAYTIME:
                    if (checkEnable(rule, now)) {
                        delayTimeRules.add(rule);
                    }
                    break;
                case EXCEPTION:
                    if (checkEnable(rule, now)) {
                        exceptonRules.add(rule);
                    }
                    break;
                case PIPELINETIMEOUT:
                    if (checkEnable(rule, now)) {
                        pipelineTimeoutRules.add(rule);
                    }
                    break;
                case PROCESSTIMEOUT:
                    if (checkEnable(rule, now)) {
                        processTimeoutRules.add(rule);
                    }
                    break;
                case PAUSED:
                    if (checkEnable(rule, now)) {
                        pausedRules.add(rule);
                    }
                    break;
                case POSITIONTIMEOUT:
                    if (checkEnable(rule, now)) {
                        positionTimeoutRules.add(rule);
                    }
                    break;
                default:
                    break;
            }
        }

        if (!delayTimeRules.isEmpty()) {
            delayStatRuleMonitor.explore(delayTimeRules);
        }

        if (!pipelineTimeoutRules.isEmpty()) {
            pipelineTimeoutRuleMonitor.explore(pipelineTimeoutRules);
        }

        if (!processTimeoutRules.isEmpty()) {
            processTimeoutRuleMonitor.explore(processTimeoutRules);
        }

        if (!positionTimeoutRules.isEmpty()) {
            positionTimeoutRuleMonitor.explore(positionTimeoutRules);
        }

        if (pausedRules.isEmpty()) {
            if (!rules.isEmpty()) {
                // 默认添加一个系统的解挂任务
                AlarmRule rule = new AlarmRule();
                rule.setId(-1L);
                rule.setPipelineId(rules.get(0).getPipelineId());
                rule.setAutoRecovery(true);
                rule.setRecoveryThresold(1);
                rule.setMonitorName(MonitorName.PAUSED);
                pausedRules.add(rule);
            }
        }

        if (!pausedRules.isEmpty()) {
            pausedRuleMonitor.explore(pausedRules);
        }

    }

    private boolean checkEnable(AlarmRule rule, Date now) {
        return rule.getPauseTime() == null || rule.getPauseTime().before(now);
    }

    public void explore() {
        throw new UnsupportedOperationException();
    }

    public void explore(Long... pipelineIds) {
        throw new UnsupportedOperationException();
    }

}
