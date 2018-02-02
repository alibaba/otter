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

package com.alibaba.otter.common.push;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.otter.common.push.media.MediaSubscribeManager;
import com.google.common.base.Function;
import com.google.common.collect.OtterMigrateMap;

/**
 * @author zebin.xuzb 2012-9-19 上午10:15:22
 * @version 4.1.0
 */
public class SubscribeManagerFactory implements ApplicationContextAware {

    private static ApplicationContext                         context        = null;

    private static final Map<SubscribeType, SubscribeManager> innerContainer = OtterMigrateMap.makeComputingMap(new Function<SubscribeType, SubscribeManager>() {

                                                                                 @Override
                                                                                 public SubscribeManager apply(SubscribeType input) {
                                                                                     return createSubsrcibeManager(input);
                                                                                 }
                                                                             });

    private static SubscribeManager createSubsrcibeManager(SubscribeType type) {
        SubscribeManager manager = null;
        if (SubscribeType.MEDIA.equals(type)) {
            manager = new MediaSubscribeManager();
            autowire(manager);
        } else {
            throw new PushException("can't createSubsrcibeManager, type : " + type + " is not supported yet");
        }

        manager.init();
        addShutdownForManager(manager);
        return manager;
    }

    private static void addShutdownForManager(final SubscribeManager manager) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                manager.shutdown();
            }
        }));
    }

    /**
     * 根据订阅类型获取相应的订阅管理器。每个订阅类型对应一个单例的订阅管理器。<br/>
     * 
     * @param type
     * @return 订阅管理器
     * @see SubscribeManager
     * @see SubscribeType
     */
    public static SubscribeManager getSubscribeManager(SubscribeType type) {
        if (type == null) {
            return null;
        }
        SubscribeManager manager = (SubscribeManager) innerContainer.get(type);
        return manager;
    }

    public static void autowire(Object obj) {
        // 重新注入一下对象
        context.getAutowireCapableBeanFactory().autowireBeanProperties(obj,
                                                                       AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
                                                                       true);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
