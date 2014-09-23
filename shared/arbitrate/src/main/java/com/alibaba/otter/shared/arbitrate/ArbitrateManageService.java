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

package com.alibaba.otter.shared.arbitrate;

import com.alibaba.otter.shared.arbitrate.impl.manage.ChannelArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.NodeArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.PipelineArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.manage.SystemArbitrateEvent;

/**
 * 仲裁器管理服务，提供给console进行干预仲裁器的行为：比如开始/停止channel同步
 * 
 * @author jianghang 2011-8-9 下午04:40:36
 */
public interface ArbitrateManageService {

    public SystemArbitrateEvent systemEvent();

    public NodeArbitrateEvent nodeEvent();

    public PipelineArbitrateEvent pipelineEvent();

    public ChannelArbitrateEvent channelEvent();

}
