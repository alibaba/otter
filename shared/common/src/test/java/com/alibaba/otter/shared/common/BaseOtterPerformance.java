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

package com.alibaba.otter.shared.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.common.utils.TestUtils;

/**
 * 性能测试基类,在原先PerformanceTesterExt基础上添加了warmup的功能，保证thread和JIT优化
 * 
 * <pre>
 * 使用方法:
 * BaseOtterPerformance tester = new BaseOtterPerformance(20, 1000, 1000);
 * try {
 *   tester.test(new BaseOtterPerformance.Job("测试") {
 *     public void execute() {
 *       try {
 *         Thread.sleep(10);
 *       } catch (InterruptedException e) {
 *       }
 *     }
 *   });
 * } catch (Warn e) {
 *   //断言失败
 * }
 * 
 * @author jianghang 2011-10-10 上午11:15:40
 * @version 4.0.0
 */
public class BaseOtterPerformance {

    private static final Logger log = LoggerFactory.getLogger(BaseOtterPerformance.class);

    // 并发线程数
    private int                 threads;
    // 单个线程循环次数
    private int                 loop;
    // 性能阀值
    private double              threshold;

    public BaseOtterPerformance(int threads, int loop, double threshold){
        this.threads = threads;
        this.loop = loop;
        this.threshold = threshold;
    }

    public void test(Job job) throws Warn {
        // 准备测试
        Statistics statistics = new Statistics(job.getName(), threads, loop);
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Callable<Long>> tasks = new ArrayList<Callable<Long>>(threads);
        List<Future<Long>> results = new ArrayList<Future<Long>>(threads);
        // 先做一下预热，为JIT优化做准备
        warmup(pool, job);
        TestUtils.restoreJvm(); // 进行GC回收
        // 执行性能测试
        for (int i = 0; i < threads; i++) {
            tasks.add(new Runner(latch, job, loop));
        }

        long start = System.currentTimeMillis();
        try {
            results = pool.invokeAll(tasks);
            latch.await();
            // 得到结果
            for (Future<Long> future : results) {
                statistics.addTime(future.get());
            }
        } catch (Exception e) {
            throw new RuntimeException("run test fail.", e);
        }
        long duration = System.currentTimeMillis() - start;
        statistics.setTotal(duration);
        // 关闭线程池
        pool.shutdown();

        // 统计结果处理
        processStatistics(statistics);
    }

    private void warmup(ExecutorService pool, Job job) {
        // 进行预热处理
        CountDownLatch warmuplatch = new CountDownLatch(threads);
        List<Callable<Long>> tasks = new ArrayList<Callable<Long>>(threads);
        int warmup = 10070;
        int loop = warmup / threads + 1;// 计算出每个thread平均需要执行的次数
        // 先进行预热，加载一些类，避免影响测试
        for (int i = 0; i < threads; i++) {
            tasks.add(new Runner(warmuplatch, job, loop));
        }
        try {
            pool.invokeAll(tasks);
            warmuplatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("run test fail.", e);
        }
    }

    /*
     * 处理统计信息
     */
    private void processStatistics(Statistics statistics) throws Warn {
        // 打印统计信息
        System.out.println(statistics);
        // 阀值判断,判断性能是否达标
        if (statistics.average() > threshold) {
            throw new Warn(threshold, statistics.average());
        }
    }

    public class Runner implements Callable<Long> {

        private CountDownLatch latch;
        private Job            job;
        private int            loop;

        public Runner(CountDownLatch latch, Job job, int loop){
            this.latch = latch;
            this.job = job;
            this.loop = loop;
        }

        public Long call() throws Exception {
            long start = System.currentTimeMillis();
            for (int i = 0; i < loop; i++) {
                try {
                    job.execute();
                } catch (Exception e) {
                    // 暂时通过log的方式打印.如果失败场景多的话,需要设置Success&Fail机制
                    log.error("run job fail:", e);
                }
            }
            long duration = System.currentTimeMillis() - start;
            latch.countDown();
            return duration;
        }

    }

    /**
     * <pre>
     * 性能统计信息
     * </pre>
     */
    public static class Statistics {

        /** 一个线程 平均响应时间 */
        public static final String  AVERAGE_PER_THREAD  = "AVERAGE_PER_THREAD";
        /** 单次请求 平均响应时间 */
        public static final String  AVERAGE_PER_REQUEST = "AVERAGE_PER_REQUEST";

        // toString pattern
        private static final String TO_STRING_PATTERN   = "Statistics [NAME:{0}; TPS:{1}; AVERAGE:{2}ms]";

        // 统计名称
        private String              name;
        // 线程数
        private int                 threads;
        // 每个线程循环数
        private int                 loop;
        // 总共耗时时间,单位毫秒
        private long                total;
        // 每个线程消耗时间,单位毫秒
        private List<Long>          times;

        public Statistics(String name, int threads, int loop){
            this.name = name;
            this.threads = threads;
            this.loop = loop;
            this.times = new ArrayList<Long>(threads);
        }

        /**
         * 得到统计名称
         * 
         * @return 统计名称
         */
        public String name() {
            return name;
        }

        /**
         * 添加线程消耗时间
         * 
         * @param time 单个线程小时的时间,单位毫秒
         */
        public void addTime(long time) {
            times.add(time);
        }

        /**
         * 设置总耗时时间
         * 
         * @param total 总耗时,单位毫秒
         */
        public void setTotal(long total) {
            this.total = total;
        }

        /**
         * 得到总耗时时间
         * 
         * @return 总耗时,单位毫秒
         */
        public long total() {
            return total;
        }

        /**
         * 得到平均响应时间
         * 
         * @return 平均响应时间,单位毫秒
         */
        public double average() {
            return average(AVERAGE_PER_REQUEST);
        }

        /**
         * 得到TPS
         * 
         * @return TPS
         */
        public long tps() {
            return (threads * loop) * 1000 / total;
        }

        /**
         * 不同类型下的平均响应时间
         * 
         * @param type {AVERAGE,AVERAGE_PER_THREAD,AVERAGE_PER_REQUEST}
         * @return 平均响应时间,单位毫秒
         */
        public double average(String type) {
            if (AVERAGE_PER_THREAD.equals(type)) {
                long loopTotal = 0;
                for (long t : times) {
                    loopTotal += t;
                }
                return ((double) loopTotal) / threads;
            }
            if (AVERAGE_PER_REQUEST.equals(type)) {
                long loopTotal = 0;
                for (long t : times) {
                    loopTotal += t;
                }
                // System.out.println(loopTotal);
                return ((double) loopTotal) / (threads * loop);
            }
            throw new IllegalArgumentException("type error.");
        }

        @Override
        public String toString() {
            return MessageFormat.format(TO_STRING_PATTERN, name(), tps(), average());
        }

    }

    /**
     * 测试单元逻辑
     */
    public static abstract class Job {

        // 测试名称
        private String name;

        public Job(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }

        /**
         * 测试逻辑
         */
        public abstract void execute();

    }

    /**
     * <pre>
     * 警告
     * 性能不达标时,抛出此异常
     * </pre>
     */
    public static class Warn extends Exception {

        private static final long serialVersionUID = -5790026554772900047L;

        private double            expected;
        private double            actual;

        public Warn(double expected, double actual){
            this.expected = expected;
            this.actual = actual;
        }

        public double getExpected() {
            return expected;
        }

        public double getActual() {
            return actual;
        }

    }

}
