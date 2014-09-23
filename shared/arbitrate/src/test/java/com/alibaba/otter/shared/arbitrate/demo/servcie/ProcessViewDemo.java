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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.math.RandomUtils;

import com.alibaba.otter.shared.arbitrate.ArbitrateViewService;
import com.alibaba.otter.shared.common.model.statistics.stage.ProcessStat;
import com.alibaba.otter.shared.common.model.statistics.stage.StageStat;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.google.common.collect.Maps;

public class ProcessViewDemo implements PipelineLifeCycle {

    private ArbitrateViewService arbitrateViewService;
    private ExecutorService      executor = Executors.newCachedThreadPool(new NamedThreadFactory("ProcessView"));
    private Map<Long, Future>    threads  = Maps.newConcurrentMap();

    public void submit(Long pipelineId) {
        if (threads.containsKey(pipelineId)) {
            throw new IllegalArgumentException("pipeline is dup!");
        }

        Future future = executor.submit(new ViewWorker(pipelineId));
        threads.put(pipelineId, future);
    }

    public void destory(Long pipelineId) {
        if (!threads.containsKey(pipelineId)) {
            throw new IllegalArgumentException("pipeline is not exist!");
        }

        Future future = threads.get(pipelineId);
        future.cancel(true);
    }

    private class ViewWorker implements Runnable {

        private Long pipelineId;

        public ViewWorker(Long pipelineId){
            this.pipelineId = pipelineId;
        }

        public void run() {
            PrintWriter print = null;
            try {
                String tmp = System.getProperty("java.io.tmpdir", "/tmp");
                File log = new File(tmp + "/process.log");
                if (log.exists() == false) {
                    log.createNewFile();
                }

                print = new PrintWriter(log);
                while (true) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    List<ProcessStat> processStats = arbitrateViewService.listProcesses(100L, pipelineId);
                    StringBuilder builder = new StringBuilder();
                    builder.append("=============================== time : " + printDate(new Date().getTime())).append("\n");

                    builder.append("pipeline : ").append(pipelineId).append("\n");
                    for (ProcessStat processStat : processStats) {
                        builder.append("\t process : ").append(processStat.getProcessId()).append("\n");

                        for (StageStat stageStat : processStat.getStageStats()) {
                            builder.append("\t\t ").append(stageStat.getStage().name());
                            builder.append("    ====> startTime [").append(printDate(stageStat.getStartTime())).append(" ]");
                            if (stageStat.getEndTime() != null) {
                                builder.append(" endTime [").append(printDate(stageStat.getEndTime())).append(" ]");
                            }
                            builder.append("\n");
                        }
                    }

                    print.println(builder.toString());
                    print.flush();

                    try {
                        Thread.sleep(500 + RandomUtils.nextInt(1000));
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace(print);
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

    public void setArbitrateViewService(ArbitrateViewService arbitrateViewService) {
        this.arbitrateViewService = arbitrateViewService;
    }

}
