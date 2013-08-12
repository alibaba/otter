package com.alibaba.otter.manager.biz.monitor;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

import org.springframework.util.ReflectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.alibaba.otter.manager.biz.monitor.impl.AbstractRuleMonitor;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;

/**
 * @version 4.1.0
 */
public class AbstractRuleMonitorInPeriodTest {

    private AbstractRuleMonitor monitor        = new AbstractRuleMonitor() {

                                                   @Override
                                                   public void explore(List<AlarmRule> rules) {
                                                       // do nothing
                                                   }

                                                   // 14:33:00
                                                   @Override
                                                   protected Calendar currentCalendar() {
                                                       Calendar calendar = Calendar.getInstance();
                                                       calendar.set(Calendar.AM_PM, Calendar.PM);
                                                       calendar.set(Calendar.HOUR, 2);
                                                       calendar.set(Calendar.MINUTE, 33);
                                                       return calendar;
                                                   }

                                               };

    private Method              inPeriodMethod = ReflectionUtils.findMethod(AbstractRuleMonitor.class, "inPeriod",
                                                                            new Class[] { String.class });

    @Test
    public void testInPeriod() {
        ReflectionUtils.makeAccessible(inPeriodMethod);
        String rule = "aaf@3452zd@qwas:213-adz@10:00-13:00,14:00-15:00";
        boolean isInPeriod = (Boolean) ReflectionUtils.invokeMethod(inPeriodMethod, monitor, new Object[] { rule });
        Assert.assertTrue(isInPeriod);
    }

    @Test
    public void testNotInPeriod() {
        ReflectionUtils.makeAccessible(inPeriodMethod);
        String rule = "aaf@3452zd@qwas:213-adz@14:40-15:00";
        boolean isInPeriod = (Boolean) ReflectionUtils.invokeMethod(inPeriodMethod, monitor, new Object[] { rule });
        Assert.assertFalse(isInPeriod);
    }

    @Test
    public void testCriticalInPeriod() {
        ReflectionUtils.makeAccessible(inPeriodMethod);
        String rule = "aaf@3452zd@qwas:213-adz@14:33-15:00";
        boolean isInPeriod = (Boolean) ReflectionUtils.invokeMethod(inPeriodMethod, monitor, new Object[] { rule });
        Assert.assertTrue(isInPeriod);
    }

    @Test
    public void testErrorFormatInPeriod() {
        ReflectionUtils.makeAccessible(inPeriodMethod);
        String rule = "aaf@3452zd@qwas:213-adz@14:331-15:00";
        boolean isInPeriod = (Boolean) ReflectionUtils.invokeMethod(inPeriodMethod, monitor, new Object[] { rule });
        Assert.assertTrue(isInPeriod);
    }

}
