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

import com.alibaba.otter.shared.arbitrate.impl.setl.LoadArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter.ArbitrateMode;

/**
 * load delegate实现
 * 
 * @author jianghang 2012-9-28 上午10:36:38
 * @version 4.1.0
 */
public class LoadDelegateArbitrateEvent extends AbstractDelegateArbitrateEvent implements LoadArbitrateEvent {

    private Map<ArbitrateMode, LoadArbitrateEvent> delegate;

    public EtlEventData await(Long pipelineId) throws InterruptedException {
        return delegate.get(chooseMode(pipelineId)).await(pipelineId);
    }

    public void single(EtlEventData data) {
        delegate.get(chooseMode(data.getPipelineId())).single(data);
    }

    public void setDelegate(Map<ArbitrateMode, LoadArbitrateEvent> delegate) {
        this.delegate = delegate;
    }

}
