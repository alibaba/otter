package com.alibaba.otter.node.etl.common.jmx;

import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.BaseOtterTest;
import com.alibaba.otter.node.etl.common.jmx.StageAggregation.AggregationItem;

public class StageAggregationTest extends BaseOtterTest {

    @Test
    public void test_normal() {
        StageAggregation aggregation = new StageAggregation(256);

        for (int i = 0; i < 128; i++) {
            long now = System.currentTimeMillis();
            aggregation.push(new AggregationItem(now - 10 - RandomUtils.nextInt(100), now));
            LockSupport.parkNanos(1000 * 1000L);
        }

        LockSupport.parkNanos(2000 * 1000 * 1000L);

        for (int i = 0; i < 200; i++) {
            long now = System.currentTimeMillis();
            aggregation.push(new AggregationItem(now - 10 - RandomUtils.nextInt(100), now));
            LockSupport.parkNanos(1000 * 1000L);
        }

        String result = aggregation.histogram();
        System.out.println(result);
    }

    @Test
    public void test_zero() {
        StageAggregation aggregation = new StageAggregation(256);
        String result = aggregation.histogram();
        System.out.println(result);
    }
}
