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

package com.alibaba.otter.manager.web.home.module.screen.api;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;

/**
 * @author zebin.xuzb @ 2012-5-18
 */
public class ChannelOp extends AbstractJsonScreen<String> {

    private final static String START = "start";
    private final static String STOP  = "stop";

    @Resource(name = "channelService")
    private ChannelService      channelService;

    public void execute(@Param("id") Long id, @Param("command") String command) {

        try {
            if (StringUtils.equalsIgnoreCase(command, START)) {
                channelService.startChannel(id);
            } else if (StringUtils.equalsIgnoreCase(command, STOP)) {
                channelService.stopChannel(id);
            } else {
                returnError("please add specfy the 'command' param.");
                return;
            }
            returnSuccess();
        } catch (Exception e) {
            String errorMsg = String.format("error happens while [%s] channel with id [%d]", command, id);
            log.error(errorMsg, e);
            returnError(errorMsg);
        }
    }
}
