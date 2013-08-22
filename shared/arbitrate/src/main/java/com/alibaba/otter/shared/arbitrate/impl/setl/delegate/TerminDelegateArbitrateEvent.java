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

package com.alibaba.otter.shared.arbitrate.impl.setl.delegate;

import java.util.Map;

import com.alibaba.otter.shared.arbitrate.impl.setl.TerminArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter.ArbitrateMode;

/**
 * termin delegate实现
 * 
 * @author jianghang 2012-9-28 上午10:42:08
 * @version 4.1.0
 */
public class TerminDelegateArbitrateEvent extends AbstractDelegateArbitrateEvent implements TerminArbitrateEvent {

    private Map<ArbitrateMode, TerminArbitrateEvent> delegate;

    public TerminEventData await(Long pipelineId) throws InterruptedException {
        return delegate.get(chooseMode(pipelineId)).await(pipelineId);
    }

    public void single(TerminEventData data) {
        delegate.get(chooseMode(data.getPipelineId())).single(data);
    }

    public void exhaust(Long pipelineId) {
        delegate.get(chooseMode(pipelineId)).exhaust(pipelineId);
    }

    public void ack(TerminEventData data) {
        delegate.get(chooseMode(data.getPipelineId())).ack(data);
    }

    public int size(Long pipelineId) {
        return delegate.get(chooseMode(pipelineId)).size(pipelineId);
    }

    public void setDelegate(Map<ArbitrateMode, TerminArbitrateEvent> delegate) {
        this.delegate = delegate;
    }

}
