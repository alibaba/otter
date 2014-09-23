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

package com.alibaba.otter.manager.biz.remote.impl;

import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.manager.biz.config.canal.CanalService;
import com.alibaba.otter.manager.biz.remote.CanalRemoteService;
import com.alibaba.otter.shared.communication.core.CommunicationRegistry;
import com.alibaba.otter.shared.communication.model.canal.CanalEventType;
import com.alibaba.otter.shared.communication.model.canal.FindCanalEvent;
import com.alibaba.otter.shared.communication.model.canal.FindFilterEvent;

/**
 * 获取对应的canal配置
 * 
 * @author jianghang 2012-8-1 下午04:44:40
 * @version 4.1.0
 */
public class CanalRemoteServiceImpl implements CanalRemoteService {

    private CanalService canalService;

    public CanalRemoteServiceImpl(){
        CommunicationRegistry.regist(CanalEventType.findCanal, this);
        CommunicationRegistry.regist(CanalEventType.findFilter, this);
    }

    public Canal onFindCanal(FindCanalEvent event) {
        String destination = event.getDestination();
        return canalService.findByName(destination);
    }

    public String onFindFilter(FindFilterEvent event) {
        // TODO 根据同步队列的需求，直接设定filter过滤
        return ".*\\..*";
    }

    public void setCanalService(CanalService canalService) {
        this.canalService = canalService;
    }

}
