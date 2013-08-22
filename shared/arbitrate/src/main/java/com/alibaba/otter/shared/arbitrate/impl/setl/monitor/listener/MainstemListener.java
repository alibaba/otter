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

/**
 * 触发一下mainstem发生切换
 * 
 * @author jianghang 2012-9-11 下午02:26:03
 * @version 4.1.0
 */
public interface MainstemListener {

    /**
     * 触发现在轮到自己做为active，需要载入上一个active的上下文数据
     */
    public void processActiveEnter();

    /**
     * 触发一下当前active模式失败
     */
    public void processActiveExit();

}
