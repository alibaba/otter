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

package com.alibaba.otter.shared.arbitrate.impl.setl;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;

/**
 * 抽象select模块的调度接口
 * 
 * @author jianghang 2012-9-27 下午09:54:53
 * @version 4.1.0
 */
public interface SelectArbitrateEvent extends ArbitrateEvent {

    public EtlEventData await(Long pipelineId) throws InterruptedException;

    public void single(EtlEventData data);
}
