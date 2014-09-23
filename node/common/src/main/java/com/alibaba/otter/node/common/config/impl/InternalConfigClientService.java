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

package com.alibaba.otter.node.common.config.impl;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;

/**
 * 内部config service
 * 
 * @author jianghang
 */
public interface InternalConfigClientService extends ConfigClientService {

    /**
     * 创建或者更新本地service的数据
     */
    public void createOrUpdateChannel(Channel channel);
}
