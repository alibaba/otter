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

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.alibaba.otter.shared.arbitrate.ArbitrateEventService;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.google.common.collect.Maps;

/**
 * select的示例代码
 * 
 * @author jianghang 2011-8-22 下午04:28:12
 */
public class TerminProcessDemo implements PipelineLifeCycle {

    private ArbitrateEventService arbitrateEventService;
    private ExecutorService       executor = Executors.newCachedThreadPool(new NamedThreadFactory("TerminProcess"));
    private Map<Long, Future>     threads  = Maps.newConcurrentMap();

    public void submit(Long pipelineId) {
        if (threads.containsKey(pipelineId)) {
            throw new IllegalArgumentException("pipeline is dup!");
        }

        Future future = executor.submit(new TermintWorker(pipelineId));
        threads.put(pipelineId, future);
    }

    public void destory(Long pipelineId) {
        if (!threads.containsKey(pipelineId)) {
            throw new IllegalArgumentException("pipeline is not exist!");
        }

        Future future = threads.get(pipelineId);
        future.cancel(true);
    }

    private class TermintWorker implements Runnable {

        private Long pipelineId;

        public TermintWorker(Long pipelineId){
            this.pipelineId = pipelineId;
        }

        public void run() {
            PrintWriter print = null;
            try {
                String tmp = System.getProperty("java.io.tmpdir", "/tmp");
                File log = new File(tmp + "/termin.log");
                if (log.exists() == false) {
                    log.createNewFile();
                }

                print = new PrintWriter(log);
                while (true) {
                    try {
                        final TerminEventData eventData = arbitrateEventService.terminEvent().await(pipelineId);
                        // 可以使用异步处理
                        TerminType terminType = eventData.getType();
                        if (terminType.isNormal()) {
                            // 更新游标，判断是否发送ack给eromanga信息
                        } else {
                            // 出现异常了，rollback对应的数据
                        }

                        arbitrateEventService.terminEvent().ack(eventData);// 处理完了，通知下仲裁器
                        StringBuilder builder = new StringBuilder();
                        builder.append("=============================== time : " + printDate(new Date().getTime())).append("\n");

                        builder.append("pipeline : ").append(pipelineId).append("\n");
                        builder.append("\t termin : ").append(eventData.getProcessId()).append(" type : ").append(eventData.getType().name()).append("\n");
                        print.println(builder);
                        print.flush();
                    } catch (InterruptedException e) {
                        break;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace(print);
            } finally {
                print.flush();
                print.close();
            }
        }

        private String printDate(Long time) {
            Date date = new Date(time);
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        }
    }

    public void setArbitrateEventService(ArbitrateEventService arbitrateEventService) {
        this.arbitrateEventService = arbitrateEventService;
    }

}
