package com.alibaba.otter.manager.biz;

import java.util.Map;

import org.jtester.annotations.SpringApplicationContext;
import org.jtester.core.TestedObject;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.testng.annotations.BeforeMethod;

import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.common.utils.TestUtils;

/**
 * @author jianghang 2011-9-16 下午02:58:37
 * @version 4.0.0
 */
@SpringApplicationContext("applicationContext.xml")
public class BaseOtterTest extends org.jtester.testng.JTester {

    @BeforeMethod
    public void setUp() {
        try {
            Map cache = (Map) TestUtils.getField(new ArbitrateFactory(), "cache");
            cache.clear();
        } catch (Exception e) {
            want.fail();
        }
    }

    protected BeanFactory getBeanFactory() {
        return (BeanFactory) TestedObject.getSpringBeanFactory();
    }

    protected void autowire(Object obj) {
        // 重新注入一下对象
        ((AutowireCapableBeanFactory) TestedObject.getSpringBeanFactory()).autowireBeanProperties(obj,
            AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
            true);
    }
}
