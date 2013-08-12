package com.alibaba.otter.shared.common.sample;

import org.springframework.beans.factory.InitializingBean;

/**
 * 测试的Mock servie1
 * 
 * @author jianghang 2010-6-2 下午12:20:05
 */
public class TestMockService1 implements InitializingBean {

    private TestMockService2 testService2;

    public String doTestMockService1() {
        return "server 1 done!";
    }

    public String doTestMockService1(String param) {
        return testService2.doTestMockService2() + param;
    }

    public void setTestService2(TestMockService2 testService2) {
        this.testService2 = testService2;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("TestMockService1 init!");
    }

}
