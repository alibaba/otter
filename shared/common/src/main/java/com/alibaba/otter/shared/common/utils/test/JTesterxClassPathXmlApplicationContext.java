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

package com.alibaba.otter.shared.common.utils.test;

import java.util.List;

import org.jtester.module.spring.JTesterSpringContext;
import org.jtester.module.tracer.TracerBeanManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.parsing.DefaultsDefinition;
import org.springframework.beans.factory.parsing.EmptyReaderEventListener;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.springframework.beans.factory.xml.DocumentDefaultsDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;

/**
 * 集成jtester，并扩展相关内容
 * 
 * <pre>
 * 扩展内容：
 * 1. 复写initBeanDefinitionReader，增加XmlBeanDefinitionReader的自定义属性，插入自定义的
 * {@link JTesterxReaderEventListener}，用于复写lazy-init=true属性
 * 2. 复写{@link AbstractRefreshableApplicationContext}的
 * customizeBeanFactory，修改allowEagerClassLoading属性为false，使得支持lazy-init=true的设置
 * 
 * <pre>
 * 
 * @author jianghang 2010-6-2 上午10:58:04
 */
public class JTesterxClassPathXmlApplicationContext extends org.jtester.module.spring.ClassPathXmlApplicationContextFactory {

    private class JTesterxSpringContext extends org.jtester.module.spring.JTesterSpringContext {

        public JTesterxSpringContext(Object testedObject, String[] configLocations, boolean ignoreNoSuchBean)
                                                                                                             throws BeansException{
            super(testedObject, configLocations, ignoreNoSuchBean);
        }

        public JTesterxSpringContext(String[] configLocations, boolean refresh, ApplicationContext parent,
                                     boolean ignoreNoSuchBean) throws BeansException{
            super(configLocations, refresh, parent, ignoreNoSuchBean);
        }

        /**
         * 下面这段本来想将spring初始化时所有的bean都置成lazy-init的模式<br>
         * 但实现中碰到问题,主要是tracer的aop初始化上出错。
         **/
        @Override
        protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
            beanDefinitionReader.setEventListener(new JTesterxReaderEventListener());
        }

        @Override
        protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
            super.customizeBeanFactory(beanFactory);
            beanFactory.setAllowEagerClassLoading(false);
        }

    }

    /**
     * 自定义spring ReaderEventListener<br>
     * 参见{@link DefaultBeanDefinitionDocumentReader} 和 {@link BeanDefinitionParserDelegate}的initDefaults方法
     * 
     * <pre>
     * 复写defaultsRegistered方法，在跑单元测试中，强制设置default-lazy-init=true属性
     * </pre>
     **/
    public static class JTesterxReaderEventListener extends EmptyReaderEventListener {

        @Override
        public void defaultsRegistered(DefaultsDefinition defaultsDefinition) {
            if (defaultsDefinition instanceof DocumentDefaultsDefinition) {
                DocumentDefaultsDefinition docDefault = (DocumentDefaultsDefinition) defaultsDefinition;
                docDefault.setLazyInit("true");
            }
        }
    }

    @Override
    public JTesterSpringContext createApplicationContext(List<String> locations, boolean ignoreNoSuchBean) {
        JTesterSpringContext c = new JTesterxSpringContext(locations.toArray(new String[0]), false, null,
                                                           ignoreNoSuchBean);

        TracerBeanManager.clear();
        return c;
    }
}
