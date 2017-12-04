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

package com.alibaba.otter.shared.arbitrate.impl.setl;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;
import com.google.common.base.Function;
import com.google.common.collect.OtterMigrateMap;

/**
 * 针对arbitrate的对象管理的工厂方法，基于pipelineId需要做对象缓存
 * 
 * @author jianghang 2011-9-20 上午11:24:19
 * @version 4.0.0
 */
public class ArbitrateFactory implements ApplicationContextAware {

    private static ApplicationContext            context = null;
    // 两层的Map接口，第一层为pipelineId，第二层为具体的资源类型class
    private static Map<Long, Map<Class, Object>> cache   = OtterMigrateMap.makeComputingMap(new Function<Long, Map<Class, Object>>() {

                                                             public Map<Class, Object> apply(final Long pipelineId) {
                                                                 return OtterMigrateMap.makeComputingMap(new Function<Class, Object>() {

                                                                     public Object apply(Class instanceClass) {
                                                                         return newInstance(pipelineId, instanceClass);
                                                                     }
                                                                 });
                                                             }
                                                         });

    private static Object newInstance(Long pipelineId, Class instanceClass) {
        Object obj = newInstance(instanceClass, pipelineId);// 通过反射调用构造函数进行初始化
        autowire(obj);
        return obj;
    }

    /**
     * 指定对应的pipelineId，获取对应的仲裁资源类型<br/>
     * 要求对应的instanceClass，都必须支持以pipelineId做为唯一参数的构造函数
     */
    public static <T extends ArbitrateLifeCycle> T getInstance(Long pipelineId, Class<T> instanceClass) {
        // Map<Class, Object> resources = cache.get(pipelineId);
        // if (resources == null) {
        // synchronized (cache) {// 锁住一下
        // if (!cache.containsKey(pipelineId)) {// double check
        // resources = new ConcurrentHashMap<Class, Object>();
        // cache.put(pipelineId, resources);
        // } else {
        // resources = cache.get(pipelineId);
        // }
        // }
        // }
        // Object obj = resources.get(instanceClass);
        // if (obj == null) {
        // synchronized (instanceClass) {// 锁住class对象
        // if (!resources.containsKey(instanceClass)) { // double check
        // obj = newInstance(instanceClass, pipelineId);// 通过反射调用构造函数进行初始化
        // autowire(obj);
        // resources.put(instanceClass, obj);
        // } else {
        // obj = resources.get(instanceClass);
        // }
        // }
        // }
        // return (T) obj;

        return (T) cache.get(pipelineId).get(instanceClass);
    }

    public static void autowire(Object obj) {
        // 重新注入一下对象
        context.getAutowireCapableBeanFactory().autowireBeanProperties(obj,
                                                                       AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
                                                                       true);
    }

    public static void destory() {
        for (Long pipelineId : cache.keySet()) {
            destory(pipelineId);
        }
    }

    /**
     * 销毁和释放对应pipelineId的仲裁资源
     * 
     * @param pipelineId
     */
    public static void destory(Long pipelineId) {
        Map<Class, Object> resources = cache.remove(pipelineId);
        if (resources != null) {
            Collection collection = resources.values();
            for (Object obj : collection) {
                if (obj instanceof ArbitrateLifeCycle) {
                    ArbitrateLifeCycle lifeCycle = (ArbitrateLifeCycle) obj;
                    lifeCycle.destory();// 调用销毁方法
                }
            }
        }
    }

    /**
     * 销毁和释放对应pipelineId的仲裁资源
     * 
     * @param pipelineId
     */
    public static <T extends ArbitrateLifeCycle> void destory(Long pipelineId, Class<T> instanceClass) {
        Map<Class, Object> resources = cache.get(pipelineId);
        if (resources != null) {
            Object obj = resources.remove(instanceClass);
            if (obj instanceof ArbitrateLifeCycle) {
                ArbitrateLifeCycle lifeCycle = (ArbitrateLifeCycle) obj;
                lifeCycle.destory();// 调用销毁方法
            }
        }
    }

    // ==================== helper method =======================

    private static Object newInstance(Class type, Long pipelineId) {
        Constructor _constructor = null;
        Object[] _constructorArgs = new Object[1];
        _constructorArgs[0] = pipelineId;

        try {
            _constructor = type.getConstructor(new Class[] { Long.class });
        } catch (NoSuchMethodException e) {
            throw new ArbitrateException("Constructor_notFound");
        }

        try {
            return _constructor.newInstance(_constructorArgs);
        } catch (Exception e) {
            throw new ArbitrateException("Constructor_newInstance_error", e);
        }

    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
