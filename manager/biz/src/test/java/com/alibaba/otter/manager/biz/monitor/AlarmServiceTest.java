package com.alibaba.otter.manager.biz.monitor;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.Test;

import com.alibaba.otter.manager.biz.BaseOtterTest;
import com.alibaba.otter.manager.biz.common.alarm.AlarmMessage;
import com.alibaba.otter.manager.biz.common.alarm.DefaultAlarmService;

public class AlarmServiceTest extends BaseOtterTest {

    @SpringBeanByName
    private DefaultAlarmService alarmService;

    @Test
    public void test_simple() {
        AlarmMessage data = new AlarmMessage();
        data.setMessage("this is test");
        data.setReceiveKey("jianghang.loujh@alibaba-inc.com");
        try {
            alarmService.doSend(data);
        } catch (Exception e) {
            want.fail(ExceptionUtils.getFullStackTrace(e));
        }
    }
}
