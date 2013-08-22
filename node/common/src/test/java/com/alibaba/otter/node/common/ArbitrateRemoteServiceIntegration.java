/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
