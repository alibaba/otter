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

package com.alibaba.otter.node.etl.extract;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.alibaba.otter.shared.common.model.config.enums.StageType;

/**
 * @author simon 2012-12-3 下午5:15:15
 * @version 4.1.0
 */
public class SetlFuture<V> extends FutureTask<V> {

    private StageType         stageType;
    private Long              processId;
    private Map<Long, Future> pendingFuture;

    public SetlFuture(StageType stageType, Long processId, Map<Long, Future> pendingFuture, Runnable runnable){
        super(runnable, null);
        this.stageType = stageType;
        this.pendingFuture = pendingFuture;
        this.pendingFuture.put(processId, this);
        this.processId = processId;
    }

    protected void done() {
        pendingFuture.remove(processId); // 完成了，将自己从pendingFuture移除
    }

    public String toString() {
        return "SetlFuture [processId=" + processId + ", stageType=" + stageType + "]";
    }

}
