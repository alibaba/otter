package com.alibaba.otter.shared.common.sample;

import org.springframework.beans.factory.InitializingBean;

/**
 * 测试的Mock servie2
 * 
 * @author jianghang 2010-6-2 下午12:20:49
 */
public class TestMockService2 implements InitializingBean {

    public String doTestMockService2() {
        return "server 2 done!";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("TestMockService2 init!");
    }
}
