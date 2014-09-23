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

package com.alibaba.otter.manager.web.home.module.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.config.parameter.SystemParameterService;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;

public class SystemParameterAction extends AbstractAction {

    @Resource(name = "systemParameterService")
    private SystemParameterService systemParameterService;

    /**
     * 修改系统参数
     */
    public void doEdit(@FormGroup("systemParameterDetailInfo") Group systemParameterDetailInfo, Navigator nav)
                                                                                                              throws Exception {

        SystemParameter systemParameter = new SystemParameter();
        systemParameterDetailInfo.setProperties(systemParameter);
        String defaultAlarmReceiver = systemParameterDetailInfo.getField("defaultAlarmReceiver").getStringValue();
        String defaultAlarmReceiverStrs[] = StringUtils.split(defaultAlarmReceiver, "=");
        if (defaultAlarmReceiverStrs.length != 2) {
            throw new ManagerException("defaultAlarmReceiver[" + defaultAlarmReceiver + "] is not valid!");
        }
        systemParameter.setDefaultAlarmReceiveKey(defaultAlarmReceiverStrs[0]);
        systemParameter.setDefaultAlarmReceiver(defaultAlarmReceiverStrs[1]);

        String alarmReceiver = systemParameterDetailInfo.getField("alarmReceiver").getStringValue();

        List<String> alarmReceivers = new ArrayList<String>();
        String alarmReceiver1[] = StringUtils.split(alarmReceiver, "\n");
        for (String alarmReceiverStr : alarmReceiver1) {
            String[] alarmReceiver2 = StringUtils.split(alarmReceiverStr, ";");
            for (String alarmReceiverStr2 : alarmReceiver2) {
                alarmReceivers.add(alarmReceiverStr2);
            }
        }

        Map<String, String> alarmReceiverMap = new LinkedHashMap<String, String>();
        for (String alarmReceiverStr : alarmReceivers) {
            String alarmReceiverStrs[] = StringUtils.split(alarmReceiverStr, "=");
            if (alarmReceiverStrs.length != 2) {
                throw new ManagerException("alarmReceiver[" + alarmReceiver + "] is not valid!");
            }

            alarmReceiverMap.put(alarmReceiverStrs[0], alarmReceiverStrs[1]);
        }
        systemParameter.setAlarmReceiver(alarmReceiverMap);
        systemParameterService.createOrUpdate(systemParameter);
        nav.redirectToLocation("systemParameter.htm?edit=true");
    }
}
