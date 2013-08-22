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

import org.apache.commons.lang.StringUtils;

import com.alibaba.otter.shared.arbitrate.exception.ArbitrateException;

/**
 * 补救数据记录
 * 
 * @author jianghang 2012-4-13 上午11:18:11
 * @version 4.0.2
 */
public class RemedyIndexEventData extends EventData {

    private static final String SPLIT            = "_";
    private static final long   serialVersionUID = 3125886367323255220L;
    private Long                pipelineId;                             // 通道id
    private Long                processId;
    private Long                startTime;
    private Long                endTime;

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

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

    // ===================== helper method =================

    public static String formatNodeName(RemedyIndexEventData eventData) {
        StringBuilder builder = new StringBuilder();
        builder.append(eventData.getProcessId()).append(SPLIT).append(eventData.getStartTime()).append(SPLIT).append(eventData.getEndTime());
        return builder.toString();
    }

    public static RemedyIndexEventData parseNodeName(String node) {
        String[] datas = StringUtils.split(node, SPLIT);
        if (datas.length != 3) {
            throw new ArbitrateException("remedy index[" + node + "] format is not correctly!");
        }

        RemedyIndexEventData eventData = new RemedyIndexEventData();
        eventData.setProcessId(Long.valueOf(datas[0]));
        eventData.setStartTime(Long.valueOf(datas[1]));
        eventData.setEndTime(Long.valueOf(datas[2]));
        return eventData;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((pipelineId == null) ? 0 : pipelineId.hashCode());
        result = prime * result + ((processId == null) ? 0 : processId.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RemedyIndexEventData)) {
            return false;
        }
        RemedyIndexEventData other = (RemedyIndexEventData) obj;
        if (endTime == null) {
            if (other.endTime != null) {
                return false;
            }
        } else if (!endTime.equals(other.endTime)) {
            return false;
        }
        if (pipelineId == null) {
            if (other.pipelineId != null) {
                return false;
            }
        } else if (!pipelineId.equals(other.pipelineId)) {
            return false;
        }
        if (processId == null) {
            if (other.processId != null) {
                return false;
            }
        } else if (!processId.equals(other.processId)) {
            return false;
        }
        if (startTime == null) {
            if (other.startTime != null) {
                return false;
            }
        } else if (!startTime.equals(other.startTime)) {
            return false;
        }
        return true;
    }

}
