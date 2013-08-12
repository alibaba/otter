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
