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

package com.alibaba.otter.shared.communication.core;

import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * 通讯服务端点，需要在每个node上部署后，就可以通过Communication工具进行数据通讯
 * 
 * @author jianghang 2011-9-9 下午04:07:51
 */
public interface CommunicationEndpoint {

    /**
     * 初始化endpint
     */
    public void initial();

    /**
     * 销毁endpoint
     */
    public void destory();

    /**
     * 接受一个消息
     * 
     * @return
     */
    public Object acceptEvent(Event event);

}
