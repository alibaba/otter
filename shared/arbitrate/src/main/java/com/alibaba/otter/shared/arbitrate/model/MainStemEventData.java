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

/**
 * 主道控制信号
 * 
 * @author jianghang 2011-8-16 下午08:29:48
 */
public class MainStemEventData extends PipelineEventData {

    private static final long serialVersionUID = 2694861930354206657L;

    public enum Status {
        /** 已追上 */
        OVERTAKE,
        /** 追赶中 */
        TAKEING;

        public boolean isOverTake() {
            return this.equals(Status.OVERTAKE);
        }

        public boolean isTaking() {
            return this.equals(Status.TAKEING);
        }
    }

    private Long    nid;
    private Status  status;       // 主道控制信号的状态，已追上或者未追上
    private boolean active = true;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getNid() {
        return nid;
    }

    public void setNid(Long nid) {
        this.nid = nid;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
