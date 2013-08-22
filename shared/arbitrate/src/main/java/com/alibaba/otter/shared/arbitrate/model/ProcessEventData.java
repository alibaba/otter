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

import java.util.Map;

/**
 * 基于process数据的信号对象
 * 
 * @author jianghang 2011-8-16 下午08:19:16
 */
public class ProcessEventData extends PipelineEventData {

    private static final long serialVersionUID = 3384175022262480571L;
    private Long              processId;                              // 同步进程id
    private Long              startTime;                              // 同步开始时间
    private Long              endTime;                                // 同步结束时间
    private Long              firstTime;                              // 第一条记录的时间

    private Long              batchId;                                // 批处理Id，对应一批处理的数据
    private Long              number;                                 // 对应调度的记录数
    private Long              size;                                   // 对应调度的数据大小
    private Map               exts;                                   // 对应的扩展数据

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
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

    public Long getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(Long firstTime) {
        this.firstTime = firstTime;
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

    public Map getExts() {
        return exts;
    }

    public void setExts(Map exts) {
        this.exts = exts;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

}
