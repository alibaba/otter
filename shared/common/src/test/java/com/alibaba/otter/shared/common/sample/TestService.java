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
