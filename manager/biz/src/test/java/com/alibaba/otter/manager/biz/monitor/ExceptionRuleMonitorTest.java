package com.alibaba.otter.manager.biz.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mockit.Mocked;

import org.jtester.annotations.SpringBeanFrom;
import org.testng.annotations.Test;

import com.alibaba.otter.manager.biz.BaseOtterTest;
import com.alibaba.otter.manager.biz.config.alarm.AlarmRuleService;
import com.alibaba.otter.manager.biz.monitor.impl.ExceptionRuleMonitor;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRuleStatus;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.alibaba.otter.shared.communication.model.arbitrate.NodeAlarmEvent;

/**
 * @author zebin.xuzb 2012-9-4 下午5:20:28
 * @version 4.1.0
 */
public class ExceptionRuleMonitorTest extends BaseOtterTest {

    private ExceptionRuleMonitor monitor = new ExceptionRuleMonitor();

    @SpringBeanFrom
    @Mocked
    private AlarmRuleService     alarmRuleService;

    @Test
    public void testSerialProcess() {
        new NonStrictExpectations() {

            {
                alarmRuleService.getAlarmRules(anyLong, AlarmRuleStatus.ENABLE);
                List<AlarmRule> rules = new ArrayList<AlarmRule>();
                AlarmRule rule = new AlarmRule();
                rule.setDescription("xxx");
                rule.setGmtCreate(new Date());
                rule.setGmtModified(new Date());
                rule.setId(1L);
                rule.setMatchValue("EXCEPTION");
                rule.setMonitorName(MonitorName.EXCEPTION);
                rule.setPipelineId(2L);
                rule.setReceiverKey("otterteam");
                rule.setStatus(AlarmRuleStatus.ENABLE);

                rules.add(rule);
                returns(rules);
            }
        };

        NodeAlarmEvent event = new NodeAlarmEvent();
        event.setMessage("pid:77 nid:5 exception:EXCEPTON,nid:5[setl:ERROR ## SelectTask processId = 644408,parallelism = 5,ProcessEnd processId = 644394 invalid]");
        event.setNid(5L);
        event.setPipelineId(2L);
        event.setTitle("EXCEPTON");
        monitor.feed(event, 2L);
    }
}
