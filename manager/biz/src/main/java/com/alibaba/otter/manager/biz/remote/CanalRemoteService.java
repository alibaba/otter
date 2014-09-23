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

package com.alibaba.otter.manager.biz.remote;

import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.shared.communication.model.canal.FindCanalEvent;
import com.alibaba.otter.shared.communication.model.canal.FindFilterEvent;

/**
 * canal远程服务接口
 * 
 * @author jianghang 2012-8-1 下午04:12:41
 * @version 4.1.0
 */
public interface CanalRemoteService {

    /**
     * 接收客户端的查询Canal请求
     */
    public Canal onFindCanal(FindCanalEvent event);

    /**
     * 接收客户端的查询filter请求
     */
    public String onFindFilter(FindFilterEvent event);
}
