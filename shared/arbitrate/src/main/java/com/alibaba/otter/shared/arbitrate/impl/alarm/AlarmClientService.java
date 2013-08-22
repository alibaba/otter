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

package com.alibaba.otter.shared.arbitrate.impl.alarm;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.arbitrate.impl.communication.ArbitrateCommmunicationClient;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.alibaba.otter.shared.communication.core.model.Callback;
import com.alibaba.otter.shared.communication.model.arbitrate.NodeAlarmEvent;

/**
 * 发送报警信息给manager
 * 
 * @author jianghang 2011-9-26 下午10:30:16
 * @version 4.0.0
 */
public class AlarmClientService {

    private static final Logger           logger         = LoggerFactory.getLogger(AlarmClientService.class);
    private static final String           MESSAGE_FORMAT = "{0}:{1}";
    private ArbitrateCommmunicationClient arbitrateCommmunicationClient;

    public void sendAlarm(Long currentNid, Long pipelineId, String title, String msg) {
        final NodeAlarmEvent event = new NodeAlarmEvent();
        event.setNid(currentNid);
        event.setTitle(MonitorName.EXCEPTION.name());
        event.setMessage(MessageFormat.format(MESSAGE_FORMAT, title, msg));
        event.setPipelineId(pipelineId);
        arbitrateCommmunicationClient.callManager(event, new Callback<Object>() {

            public void call(Object result) {
                logger.info("##callManager successed! event:[{}]", event.toString());
            }

        });
    }

    // =============== setter / getter ==================

    public void setArbitrateCommmunicationClient(ArbitrateCommmunicationClient arbitrateCommmunicationClient) {
        this.arbitrateCommmunicationClient = arbitrateCommmunicationClient;
    }

}
