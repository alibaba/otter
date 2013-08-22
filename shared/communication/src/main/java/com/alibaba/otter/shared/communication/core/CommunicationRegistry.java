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

package com.alibaba.otter.shared.communication.core;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.communication.core.model.EventType;

/**
 * 注册中心,针对每个事件允许绑定一个对象处理,类似于webx中的Action事件处理
 * 
 * @author jianghang
 */
public class CommunicationRegistry {

    private static final Logger           logger = LoggerFactory.getLogger(CommunicationRegistry.class);

    // 定义事件和动作的关联关系
    private static Map<EventType, Object> table  = new ConcurrentHashMap<EventType, Object>();

    /**
     * 注册一个事件对应的处理对象
     * 
     * @param eventType
     * @param action
     */
    public static void regist(EventType eventType, Object action) {
        if (logger.isInfoEnabled()) {
            logger.info(" Regist " + action + " For " + eventType);
        }

        if (table.containsKey(eventType)) {
            if (logger.isWarnEnabled()) {
                logger.warn(" EventType " + eventType + " is already exist!");
            }
        }
        table.put(eventType, action);
    }

    /**
     * 注册一组事件处理对象
     * 
     * @param eventType
     * @param action
     */
    public static void regist(EventType[] eventTypes, Object action) {
        if (eventTypes != null) {
            for (EventType eventType : eventTypes) {
                regist(eventType, action);
            }
        }
    }

    /**
     * 注册一个事件处理对象
     * 
     * @param eventType
     */
    public static void unregist(EventType eventType) {
        if (logger.isInfoEnabled()) {
            logger.info(" Un Regist EventType : " + eventType);
        }

        table.remove(eventType);
    }

    /**
     * 注销调一个Action
     * 
     * @param action
     * @return
     */
    public static void unregist(Object action) {
        if (logger.isInfoEnabled()) {
            logger.info(" Un Regist Action : " + action);
        }

        if (action != null) {
            for (Iterator<Map.Entry<EventType, Object>> iter = table.entrySet().iterator(); iter.hasNext();) {
                Map.Entry<EventType, Object> entry = iter.next();
                if (action == entry.getValue()) {
                    if (logger.isInfoEnabled()) {
                        logger.info(" Find " + entry.getKey() + " For : " + action);
                    }

                    iter.remove();
                }
            }
        }
    }

    /**
     * 获得事件对应的Action
     * 
     * @param eventType
     * @return
     */
    public static Object getAction(EventType eventType) {
        return table.get(eventType);
    }
}
