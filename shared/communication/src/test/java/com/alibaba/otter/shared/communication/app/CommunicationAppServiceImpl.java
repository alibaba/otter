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

package com.alibaba.otter.shared.communication.app;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.otter.shared.communication.app.event.AppCreateEvent;
import com.alibaba.otter.shared.communication.app.event.AppDeleteEvent;
import com.alibaba.otter.shared.communication.app.event.AppUpdateEvent;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * @author jianghang 2011-9-13 下午08:36:01
 */
public class CommunicationAppServiceImpl implements CommunicationAppService {

    private Map<String, Event> events = new ConcurrentHashMap<String, Event>();

    public Event handleEvent(Event event) {
        throw new IllegalArgumentException();
    }

    public boolean onCreate(AppCreateEvent event) {
        events.put(event.getType().name(), event);
        return true;
    }

    public boolean onDelete(AppDeleteEvent event) {
        events.remove(event.getType().name());
        return true;
    }

    public boolean onUpdate(AppUpdateEvent event) {
        events.put(event.getType().name(), event);
        return true;
    }

}
