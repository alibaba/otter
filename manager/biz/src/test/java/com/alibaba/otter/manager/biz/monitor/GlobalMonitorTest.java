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

package com.alibaba.otter.manager.biz.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Mocked;

import org.jtester.annotations.SpringBeanByName;
import org.jtester.annotations.SpringBeanFrom;
import org.testng.annotations.Test;

import com.alibaba.otter.manager.biz.BaseOtterTest;
import com.alibaba.otter.manager.biz.config.alarm.AlarmRuleService;
import com.alibaba.otter.manager.biz.monitor.impl.AbstractRuleMonitor;
import com.alibaba.otter.manager.biz.monitor.impl.GlobalMonitor;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRuleStatus;

/**
 * @author zebin.xuzb 2012-9-4 下午5:20:28
 * @version 4.1.0
 */
public class GlobalMonitorTest extends BaseOtterTest {

    private Monitor          normalPipelineMonitor    = new AbstractRuleMonitor() {

                                                          @Override
                                                          public void explore(List<AlarmRule> rules) {
                                                              System.out.println(" normal monitor executed");
                                                          }
                                                      };

    private Monitor          exceptionPipelineMonitor = new AbstractRuleMonitor() {

                                                          @Override
                                                          public void explore(List<AlarmRule> rules) {
                                                              throw new RuntimeException(
                                                                                         " exception happens in monitor executed");
                                                          }
                                                      };

    @SpringBeanFrom
    @Mocked
    private AlarmRuleService alarmRuleService;

    @SpringBeanByName
    private GlobalMonitor    globalMonitor;

    @Test
    public void testSerialProcess() {
        new NonStrictExpectations() {

            {
                alarmRuleService.getAlarmRules(AlarmRuleStatus.ENABLE);
                Map<Long, List<AlarmRule>> allRules = new HashMap<Long, List<AlarmRule>>();
                for (long i = 0; i < 10; i++) {
                    List<AlarmRule> rules = new ArrayList<AlarmRule>();
                    for (int j = 0; j < 5; j++) {
                        rules.add(new AlarmRule());
                    }
                    allRules.put(i + 1, rules);
                }
                returns(allRules);
            }

        };

        globalMonitor.setNeedConcurrent(false);
        globalMonitor.setPipelineMonitor(normalPipelineMonitor);
        globalMonitor.explore();
    }

    @Test
    public void testConcurrentProcess() {
        new NonStrictExpectations() {

            {
                alarmRuleService.getAlarmRules(AlarmRuleStatus.ENABLE);
                Map<Long, List<AlarmRule>> allRules = new HashMap<Long, List<AlarmRule>>();
                for (long i = 0; i < 10; i++) {
                    List<AlarmRule> rules = new ArrayList<AlarmRule>();
                    for (int j = 0; j < 5; j++) {
                        rules.add(new AlarmRule());
                    }
                    allRules.put(i + 1, rules);
                }
                returns(allRules);
            }

        };

        globalMonitor.setNeedConcurrent(true);
        globalMonitor.setPipelineMonitor(normalPipelineMonitor);
        globalMonitor.explore();
    }

    @Test
    public void testConcurrentProcessWithException() {
        new NonStrictExpectations() {

            {
                alarmRuleService.getAlarmRules(AlarmRuleStatus.ENABLE);
                Map<Long, List<AlarmRule>> allRules = new HashMap<Long, List<AlarmRule>>();
                for (long i = 0; i < 10; i++) {
                    List<AlarmRule> rules = new ArrayList<AlarmRule>();
                    for (int j = 0; j < 5; j++) {
                        rules.add(new AlarmRule());
                    }
                    allRules.put(i + 1, rules);
                }
                returns(allRules);
            }

        };

        globalMonitor.setNeedConcurrent(true);
        globalMonitor.setPipelineMonitor(exceptionPipelineMonitor);
        try {
            globalMonitor.explore();
        } catch (Exception e) {
            return;
        }
        throw new IllegalStateException("unreached code");
    }

}
