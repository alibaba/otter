package com.alibaba.otter.node.etl.load;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.load.loader.weight.WeightController;
import com.alibaba.otter.node.etl.BaseOtterTest;

public class WeightControllerTest extends BaseOtterTest {

    @Test
    public void test_simple() {
        int thread = 10;
        int count = 10;
        WeightController controller = new WeightController(thread);
        CountDownLatch latch = new CountDownLatch(thread);
        WeightWorkerTest[] workers = new WeightWorkerTest[thread];
        for (int i = 0; i < thread; i++) {
            int[] weights = new int[count];
            for (int j = 0; j < count; j++) {
                weights[j] = RandomUtils.nextInt(count);
            }
            workers[i] = new WeightWorkerTest(i, weights, controller, latch);
        }

        for (int i = 0; i < thread; i++) {
            workers[i].start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            want.fail();
        }
    }

}

class WeightWorkerTest extends Thread {

    public long getId() {
        return id;
    }

    private List<Long>       weights;
    private WeightController controller;
    private CountDownLatch   latch;
    private int              id;

    public WeightWorkerTest(int id, int[] weights, WeightController controller, CountDownLatch latch){
        this.id = id;
        this.controller = controller;
        this.weights = new ArrayList<Long>();
        this.latch = latch;
        for (int i = 0; i < weights.length; i++) {
            this.weights.add(Long.valueOf(weights[i]));
        }
        Collections.sort(this.weights);
    }

    public void run() {
        try {
            controller.start(weights);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        for (Long weight : weights) {
            try {
                controller.await(weight);
                System.out.println(id + " : " + weight);
                Thread.sleep(50);
                controller.single(weight);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        latch.countDown();
    }
}
