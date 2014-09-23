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

package com.alibaba.otter.shared.arbitrate.impl.setl.rpc;

import com.alibaba.otter.shared.arbitrate.impl.setl.TerminArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.TerminZooKeeperArbitrateEvent;

/**
 * 基于rpc的仲裁调度的termin信号处理，直接使用zookeeper的现有处理机制
 * 
 * @author jianghang 2012-9-29 上午11:06:11
 * @version 4.1.0
 */
public class TerminRpcArbitrateEvent extends TerminZooKeeperArbitrateEvent implements TerminArbitrateEvent {

}
