package com.alibaba.otter.node.common;

import java.util.concurrent.TimeUnit;

import org.jtester.annotations.SpringBeanByName;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.shared.arbitrate.impl.alarm.AlarmClientService;

/**
 * 测试下仲裁器报警处理
 * 
 * @author jianghang 2011-11-29 上午09:52:18
 * @version 4.0.0
 */
public class ArbitrateRemoteServiceIntegration extends BaseOtterTest {

    @SpringBeanByName
    private ConfigClientService configClientService;

    @SpringBeanByName
    private AlarmClientService  alarmClientService;

    @BeforeClass
    public void initial() {
        System.setProperty("nid", "1");
    }

    @Test
    public void test_send() {
        alarmClientService.sendAlarm(3L, 1L, null, "load is interrupt!");

        try {
            TimeUnit.MILLISECONDS.sleep(10000L);
        } catch (InterruptedException e) {
            want.fail();
        }
    }
}
