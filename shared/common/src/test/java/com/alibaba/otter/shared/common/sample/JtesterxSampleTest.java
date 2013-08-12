package com.alibaba.otter.shared.common.sample;

import mockit.Mocked;
import mockit.Verifications;

import org.jtester.annotations.SpringBeanByName;
import org.jtester.annotations.SpringBeanFrom;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.common.BaseOtterTest;

/**
 * 标准单元测试 示例1
 * 
 * @author jianghang 2010-6-2 上午11:45:07
 */
public class JtesterxSampleTest extends BaseOtterTest {

    @SpringBeanByName
    private TestService      testService;

    @SpringBeanByName
    private TestMockService1 testService1;

    @Mocked
    @SpringBeanFrom
    private TestMockService2 testService2;

    @SpringBeanByName
    private TestMockService3 testService3;

    @Test
    public void testExpectService2() {
        final String MOCK_SERVICE2 = "mock service 2 done!";

        // new NonStrictExpectations和 Expectations区别： Expectations只允许调用被mock的方法
        new NonStrictExpectations(TestMockService2.class) {

            {
                testService2.doTestMockService2();
                returns(MOCK_SERVICE2);
            }
        };

        want.string(testService.doTestService1("test 1")).start(MOCK_SERVICE2);

        new Verifications(1) { // 检查是否执行了两次

            {
                testService2.doTestMockService2();
            }
        };
    }

    /**
     * 验证mock行为是否正确
     */
    @Test
    public void testExpect() {
        final String MOCK_SERVICE1 = "mock service 1 done!";
        final String MOCK_SERVICE2 = "mock service 2 done!";

        // new NonStrictExpectations和 Expectations区别： Expectations只允许调用被mock的方法
        new NonStrictExpectations(TestMockService1.class, TestMockService2.class) {

            {
                testService1.doTestMockService1();
                returns(MOCK_SERVICE1);

                testService2.doTestMockService2();
                returns(MOCK_SERVICE2);
            }
        };

        want.string(testService.doTestService1("test 1")).start(MOCK_SERVICE2);
        want.string(testService.doTestService2()).isEqualTo(MOCK_SERVICE2);

        new Verifications(2) { // 检查是否执行了两次

            {
                testService2.doTestMockService2();
            }
        };
    }

    /**
     * 验证执行路径是否正确
     */
    @Test
    public void testVerications() {
        want.string(testService3.doTestMockService3()).isEqualTo("server 3 done!");
        new Verifications() {

            {
                testService3.doTestMockService3();
            }
        };
    }
}
