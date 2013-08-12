package com.alibaba.otter.shared.common.sample;

import org.springframework.beans.factory.InitializingBean;

/**
 * 测试的Mock servie2,测试lazy-init=true是否正常
 * 
 * @author jianghang 2010-6-2 下午12:20:49
 */
public class TestMockService3 implements InitializingBean {

    public String doTestMockService3() {
        return "server 3 done!";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("TestMockService3 init!");
    }
}
