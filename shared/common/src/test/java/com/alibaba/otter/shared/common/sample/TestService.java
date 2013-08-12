package com.alibaba.otter.shared.common.sample;

/**
 * @author jianghang 2010-6-2 下午12:19:41
 */
public class TestService {

    private TestMockService1 testService1;
    private TestMockService2 testService2;

    public String doTestService1(String param) {
        return testService1.doTestMockService1(param);
    }

    public String doTestService2() {
        return testService2.doTestMockService2();
    }

    public void setTestService1(TestMockService1 testService1) {
        this.testService1 = testService1;
    }

    public void setTestService2(TestMockService2 testService2) {
        this.testService2 = testService2;
    }

}
