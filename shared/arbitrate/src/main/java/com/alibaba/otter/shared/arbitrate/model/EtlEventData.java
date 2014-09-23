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
 * S.E.T.L的数据传递对象
 * 
 * @author jianghang 2011-8-22 下午04:33:56
 */
public class EtlEventData extends ProcessEventData {

    private static final long serialVersionUID = -639227151519007664L;
    private Long              currNid;                                // 当前节点
    private Long              nextNid;                                // 下一个节点
    private Object            desc;                                   // 对应的pipe描述信息

    public Long getNextNid() {
        return nextNid;
    }

    public void setNextNid(Long nextNid) {
        this.nextNid = nextNid;
    }

    public Object getDesc() {
        return desc;
    }

    public void setDesc(Object desc) {
        this.desc = desc;
    }

    public Long getCurrNid() {
        return currNid;
    }

    public void setCurrNid(Long currNid) {
        this.currNid = currNid;
    }

}
