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

package com.alibaba.otter.shared.arbitrate.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * eromanga中取出的一批数据需要划分成多个process进行同步，保存process的数目和每个process同步的数据量,以及这一批数据同步是否成功.
 * 
 * @author jianghang 2011-10-18 上午10:16:12
 * @version 4.0.0
 */
public class SyncStatusEventData extends PipelineEventData {

    private static final long serialVersionUID = -1755817244279698216L;
    private List<SyncStatus>  status           = new ArrayList<SyncStatus>();
    /**
     * 初始值为并行度,在ProcessEndTask收到一个process的ack以后，就减1，如果=0时，就可以开启SelectConsumerTask去取下一批数据了.
     */
    private long              parallelism;

    public void decParallelism() {
        --parallelism;
    }

    public void addParallelism() {
        ++parallelism;
    }

    public List<SyncStatus> getStatus() {
        return status;
    }

    public void setStatus(List<SyncStatus> status) {
        this.status = status;
    }

    public void addStatus(SyncStatus status) {
        this.status.add(status);
    }

    public long getParallelism() {
        return parallelism;
    }

    public void setParallelism(long parallelism) {
        this.parallelism = parallelism;
    }

    /**
     * @author xiaoqing.zhouxq
     */
    public static class SyncStatus implements Serializable {

        private static final long serialVersionUID  = 794565950364625433L;

        public static final long  DEFAULT_PROCESSID = -1;

        /**
         * 划分成的多个process同步是否成功,如果成功，给eromanga发送ack，如果失败，从eromanga中取同一批数据， 并且过滤掉已经同步成功的process的数据.
         */
        private boolean           status;

        /**
         * 每个process需要同步的数据量.
         */
        private int               processDataCount;

        private long              processId         = DEFAULT_PROCESSID;

        public SyncStatus(){

        }

        public SyncStatus(boolean status, int processDataCount){
            this.status = status;
            this.processDataCount = processDataCount;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public int getProcessDataCount() {
            return processDataCount;
        }

        public void setProcessDataCount(int processDataCount) {
            this.processDataCount = processDataCount;
        }

        public long getProcessId() {
            return processId;
        }

        public void setProcessId(long processId) {
            this.processId = processId;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
        }
    }
}
