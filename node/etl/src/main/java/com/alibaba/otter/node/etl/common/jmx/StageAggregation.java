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

import java.util.concurrent.atomic.AtomicLong;

/**
 * 统计每个stage的运行信息
 * 
 * @author jianghang 2012-5-29 下午02:32:08
 * @version 4.0.2
 */
public class StageAggregation {

    private static final String HISTOGRAM_FORMAT = "{total:%s,count:%s,maximum:%s,minimum:%s,average:%s,tps:%s,tpm:%s}";
    private static final Long   ONE_SECOND       = 1000L;
    private static final Long   ONE_MINUTE       = 60 * 1000L;
    private int                 bufferSize       = 1 * 1024;
    private int                 indexMask;
    private AggregationItem[]   table;
    private AtomicLong          sequence         = new AtomicLong(-1);

    public StageAggregation(int bufferSize){
        if (Integer.bitCount(bufferSize) != 1) {
            throw new IllegalArgumentException("bufferSize must be a power of 2");
        }
        this.bufferSize = bufferSize;
        indexMask = this.bufferSize - 1;
        table = new AggregationItem[this.bufferSize];
    }

    public void push(AggregationItem aggregation) {
        long seq = sequence.incrementAndGet();
        table[getIndex(seq)] = aggregation;
    }

    /**
     * 返回当前stage处理次数
     */
    public Long count() {
        return sequence.get();
    }

    /**
     * 平均处理时间
     */
    public String histogram() {
        Long costs = 0L;
        Long items = 0L;
        Long max = 0L;
        Long min = Long.MAX_VALUE;
        Long tps = 0L;
        Long tpm = 0L;
        Long avg = 0L;

        Long lastTime = 0L;
        Long tpsCount = 0L;// 记录每秒的请求数，临时变量
        Long tpsTotal = 0L;// 总tps数多少
        Long tpsSecond = 0L;// 多少秒中有数据

        Long tpmCount = 0L; // 记录每分钟的请求数，临时变量
        Long tpmTotal = 0L; // 总tps数多少
        Long tpmMinute = 0L;// 多少分钟有数据
        for (int i = 0; i < table.length; i++) {
            AggregationItem aggregation = table[i];
            if (aggregation != null) {
                Long cost = aggregation.getEndTime() - aggregation.getStartTime();
                items += 1;
                costs += cost;
                if (cost > max) {
                    max = cost;
                }
                if (cost < min) {
                    min = cost;
                }

                if (lastTime != 0) {
                    if (lastTime > aggregation.getEndTime() - ONE_SECOND) {// 说明在同一秒
                        tpsCount++;
                    } else {
                        tpsTotal += tpsCount;
                        tpsSecond++;
                        tpsCount = 0L;
                    }

                    if (lastTime > aggregation.getEndTime() - ONE_MINUTE) {// 说明在同一分钟
                        tpmCount++;
                    } else {
                        tpmTotal += tpmCount;
                        tpmMinute++;
                        tpmCount = 0L;
                    }

                }

                lastTime = aggregation.getEndTime();
            }
        }
        // 设置一下最后一批tps/m统计信息
        tpsTotal += tpsCount;
        tpsSecond++;
        tpsCount = 0L;
        tpmTotal += tpmCount;
        tpmMinute++;
        tpmCount = 0L;

        if (items != 0) {
            avg = costs / items;
        }

        if (tpsSecond != 0) {
            tps = tpsTotal / tpsSecond;
        }

        if (tpmMinute != 0) {
            tpm = tpmTotal / tpmMinute;
        }

        if (min == Long.MAX_VALUE) {
            min = 0L;
        }

        return String.format(HISTOGRAM_FORMAT, new Object[] { sequence.get() + 1, items, max, min, avg, tps, tpm });
    }

    private int getIndex(long sequcnce) {
        return (int) sequcnce & indexMask;
    }

    public static class AggregationItem {

        private Long startTime; // 一次请求的起始时间
        private Long endTime;  // 一次请求的结束时间
        private Long number;   // 一次请求的处理数量
        private Long size;     // 一次请求的处理大小

        public AggregationItem(Long startTime, Long endTime, Long number, Long size){
            this.startTime = startTime;
            this.endTime = endTime;
            this.number = number;
            this.size = size;
        }

        public AggregationItem(Long startTime, Long endTime){
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public Long getStartTime() {
            return startTime;
        }

        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

        public Long getEndTime() {
            return endTime;
        }

        public void setEndTime(Long endTime) {
            this.endTime = endTime;
        }

        public Long getNumber() {
            return number;
        }

        public void setNumber(Long number) {
            this.number = number;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }

    }
}
