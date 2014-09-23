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

package com.alibaba.otter.shared.arbitrate.impl.setl.monitor.listener;

import java.util.List;

/**
 * dead node的监控处理实现，运行在启动了mainStem的单节点上，避免多个节点同时处理，所以抽取了Listener
 * 
 * @author jianghang 2011-9-26 下午10:33:42
 * @version 4.0.0
 */
public interface NodeListener {

    /**
     * 触发process变化，传递了变化后最新的processIds列表
     */
    public void processChanged(List<Long> aliveNodes);

}
