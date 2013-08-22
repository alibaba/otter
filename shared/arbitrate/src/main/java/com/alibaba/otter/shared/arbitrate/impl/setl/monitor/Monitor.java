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

package com.alibaba.otter.shared.arbitrate.impl.setl.monitor;

/**
 * Arbitrate Monitor的统一接口定义，允许进行数据的reload<br/>
 * 在并发往zookeeper写数据，通过Watcher进行监听时，Watcher响应到重新注册Watcher这段时间的数据不能得到响应， 所以需要定时进行reload，避免死锁
 * 
 * @author jianghang 2011-9-19 下午02:51:36
 * @version 4.0.0
 */
public interface Monitor {

    public void reload();

}
