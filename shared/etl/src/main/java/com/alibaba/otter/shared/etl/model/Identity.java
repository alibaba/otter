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

package com.alibaba.otter.shared.etl.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * process标识，用于区分每个process.
 * 
 * @author xiaoqing.zhouxq 2011-8-9 上午10:49:14
 */
public class Identity implements Serializable {

    private static final long serialVersionUID = -4551215214079451994L;

    private long              channelId;

    private long              pipelineId;

    private long              processId;

    public Identity(){

    }

    public Identity(Long channelId, Long pipelineId, Long processId){
        this.channelId = channelId;
        this.pipelineId = pipelineId;
        this.processId = processId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (channelId ^ (channelId >>> 32));
        result = prime * result + (int) (pipelineId ^ (pipelineId >>> 32));
        result = prime * result + (int) (processId ^ (processId >>> 32));
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
        if (!(obj instanceof Identity)) {
            return false;
        }
        Identity other = (Identity) obj;
        if (channelId != other.channelId) {
            return false;
        }
        if (pipelineId != other.pipelineId) {
            return false;
        }
        if (processId != other.processId) {
            return false;
        }
        return true;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
