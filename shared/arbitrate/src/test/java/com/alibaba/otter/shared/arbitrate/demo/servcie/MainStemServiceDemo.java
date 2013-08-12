package com.alibaba.otter.shared.arbitrate.demo.servcie;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.alibaba.otter.shared.arbitrate.ArbitrateEventService;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.google.common.collect.Maps;

/**
 * @author jianghang 2011-9-22 下午04:33:34
 * @version 4.0.0
 */
public class MainStemServiceDemo implements PipelineLifeCycle {

    private ArbitrateEventService arbitrateEventService;
    private ExecutorService       executor = Executors.newCachedThreadPool();
    private Map<Long, Future>     threads  = Maps.newConcurrentMap();

    public MainStemServiceDemo(){

    }

    public void submit(Long pipelineId) {
        if (threads.containsKey(pipelineId)) {
            throw new IllegalArgumentException("pipeline is dup!");
        }

        Future future = executor.submit(new MainStemWorker(pipelineId));
        threads.put(pipelineId, future);
    }

    public void destory(Long pipelineId) {
        if (!threads.containsKey(pipelineId)) {
            throw new IllegalArgumentException("pipeline is not exist!");
        }

        Future future = threads.get(pipelineId);
        future.cancel(true);
    }

    private class MainStemWorker implements Runnable {

        private Long pipelineId;

        public MainStemWorker(Long pipelineId){
            this.pipelineId = pipelineId;
        }

        @Override
        public void run() {
            try {
                arbitrateEventService.mainStemEvent().await(pipelineId);
                Thread.sleep(1000L);
                MainStemEventData eventData = new MainStemEventData();
                eventData.setPipelineId(pipelineId);
                eventData.setStatus(MainStemEventData.Status.OVERTAKE);
                arbitrateEventService.mainStemEvent().single(eventData);
                while (true) {
                    // 处理eromanga的数据，这里通过sleep进行模拟
                    Thread.sleep(10 * 1000);
                }
            } catch (InterruptedException e) {
                // 退出
            }
        }
    }

    public void setArbitrateEventService(ArbitrateEventService arbitrateEventService) {
        this.arbitrateEventService = arbitrateEventService;
    }

}
