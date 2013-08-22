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
