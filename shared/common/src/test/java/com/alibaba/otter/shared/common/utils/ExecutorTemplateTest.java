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

package com.alibaba.otter.shared.common.utils;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.common.BaseOtterTest;
import com.alibaba.otter.shared.common.utils.thread.ExecutorTemplate;

public class ExecutorTemplateTest extends BaseOtterTest {

    private ExecutorTemplate template = new ExecutorTemplate();

    @BeforeTest
    public void setUp() {
        try {
            template.afterPropertiesSet();
        } catch (Exception e) {
            want.fail(e.getMessage());
        }
    }

    @AfterTest
    public void tearDown() {
        try {
            template.destroy();
        } catch (Exception e) {
            want.fail(e.getMessage());
        }
    }

    @Test
    public void testSimple() {
        template.start();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            template.submit(new Runnable() {

                public void run() {
                    try {
                        Thread.sleep(RandomUtils.nextInt(1000) + 1000);
                    } catch (InterruptedException e) {
                    }
                }
            });
        }

        template.waitForResult();
        long end = System.currentTimeMillis();
        System.out.println("cost : " + (end - start));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testException() {
        template.start();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            template.submit(new Runnable() {

                public void run() {
                    if (RandomUtils.nextBoolean()) {
                        try {
                            Thread.sleep(RandomUtils.nextInt(5000) + 5000);
                        } catch (InterruptedException e) {
                            System.out.println("i'm cancel");
                        }
                    } else {
                        throw new RuntimeException("i'm error");
                    }
                }
            });
        }

        template.waitForResult();
        long end = System.currentTimeMillis();
        System.out.println("cost : " + (end - start));
    }
}
