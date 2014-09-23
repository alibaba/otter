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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.math.RandomUtils;

import com.alibaba.otter.shared.arbitrate.ArbitrateEventService;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.google.common.collect.Maps;

/**
 * transform的示例代码
 * 
 * @author jianghang 2011-8-22 下午04:28:12
 */
public class TransformServiceDemo implements PipelineLifeCycle {

    private ArbitrateEventService arbitrateEventService;
    private ExecutorService       executor = Executors.newCachedThreadPool(new NamedThreadFactory("transformService"));
    private Map<Long, Future>     threads  = Maps.newConcurrentMap();

    public void submit(Long pipelineId) {
        if (threads.containsKey(pipelineId)) {
            throw new IllegalArgumentException("pipeline is dup!");
        }

        Future future = executor.submit(new transformWorker(pipelineId));
        threads.put(pipelineId, future);
    }

    public void destory(Long pipelineId) {
        if (!threads.containsKey(pipelineId)) {
            throw new IllegalArgumentException("pipeline is not exist!");
        }

        Future future = threads.get(pipelineId);
        future.cancel(true);
    }

    private class transformWorker implements Runnable {

        private ExecutorService executor = Executors.newFixedThreadPool(10, new NamedThreadFactory("transformWorker"));
        private Long            pipelineId;

        public transformWorker(Long pipelineId){
            this.pipelineId = pipelineId;
        }

        public void run() {
            while (true) {
                try {
                    // 注意single时需传递同一个eventData对象，里面带着对应的nextNid信息
                    final EtlEventData eventData = arbitrateEventService.transformEvent().await(pipelineId);
                    // 提交对应的任务工作任务
                    executor.submit(new Callable() {

                        @Override
                        public Object call() throws Exception {
                            Long nextNid = eventData.getNextNid();
                            // 业务处理
                            Thread.sleep(500 + RandomUtils.nextInt(2000)); // sleep一下代表处理业务

                            if (isLocal(nextNid)) {// 判断是否本地
                                // 调用本地的pipe工具进行数据处理
                                eventData.setDesc(new Object());// 并设置相关信息
                            } else {
                                // 调用HTTP的pipe工具进行数据处理
                                eventData.setDesc(new Object());// 并设置相关信息
                            }

                            // 处理完成，通知一下
                            arbitrateEventService.transformEvent().single(eventData);
                            return true;
                        }

                    });

                } catch (InterruptedException e) {
                    System.out.printf("Pipeline [%s] transform is Interrupted", pipelineId);
                    // 出现中断后就退出
                    break;
                } catch (Exception e) {
                    System.out.printf("Pipeline [%s] Select is error", pipelineId);
                    e.printStackTrace();
                }
            }
        }

        private boolean isLocal(Long nid) {
            // Long currentId = OtterClientServicesLocator.getConfigClientService().currentNode()
            // .getId();
            // return currentId.equals(nid);
            return true;
        }

    }

    public void setArbitrateEventService(ArbitrateEventService arbitrateEventService) {
        this.arbitrateEventService = arbitrateEventService;
    }

}
