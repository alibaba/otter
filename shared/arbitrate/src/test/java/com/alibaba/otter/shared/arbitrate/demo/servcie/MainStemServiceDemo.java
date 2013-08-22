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
