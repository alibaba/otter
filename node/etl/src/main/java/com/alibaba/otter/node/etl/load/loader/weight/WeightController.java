package com.alibaba.otter.node.etl.load.loader.weight;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 权重控制器
 * 
 * @author jianghang 2011-11-2 上午10:34:19
 * @version 4.0.0
 */
public class WeightController {

    private AtomicInteger       latch;
    private WeightBarrier       barrier;
    private BlockingQueue<Long> weights = new PriorityBlockingQueue<Long>();

    public WeightController(int load){
        latch = new AtomicInteger(load);
        barrier = new WeightBarrier(Integer.MIN_VALUE);
    }

    /**
     * 每个loader任务报告启动的第一个任务的weight
     * 
     * @throws InterruptedException
     */
    public synchronized void start(List<Long> weights) throws InterruptedException {
        for (int i = 0; i < weights.size(); i++) {
            this.weights.add(weights.get(i));
        }

        int number = latch.decrementAndGet();
        if (number == 0) {
            Long initWeight = this.weights.peek();
            if (initWeight != null) {
                barrier.single(initWeight);
            }
        }
    }

    /**
     * 等待自己当前的weight任务可以被执行
     * 
     * @throws InterruptedException
     */
    public void await(long weight) throws InterruptedException {
        barrier.await(weight);
    }

    /**
     * 等待自己当前的weight任务可以被执行,带超时控制
     * 
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public void await(long weight, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        barrier.await(weight, timeout, unit);
    }

    /**
     * 通知下一个weight任务可以被执行
     * 
     * @throws InterruptedException
     */
    public synchronized void single(long weight) throws InterruptedException {
        this.weights.remove(weight);
        // 触发下一个可运行的weight
        Long nextWeight = this.weights.peek();
        if (nextWeight != null) {
            barrier.single(nextWeight);
        }
    }
}
