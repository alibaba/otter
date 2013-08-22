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

package com.alibaba.otter.shared.arbitrate.setl.event;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.alibaba.otter.shared.arbitrate.impl.setl.MainStemArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.setl.BaseStageTest;

/**
 * 提供mainStem信号的初始化
 * 
 * @author jianghang 2011-9-22 下午05:30:08
 * @version 4.0.0
 */
public class BaseArbitrateEventTest extends BaseStageTest {

    protected MainStemArbitrateEvent mainStemEvent;

    @BeforeMethod
    public void setUp() {
        nodeEvent.init(nid);
        channelEvent.start(channelId);//启动
    }

    @AfterMethod
    public void tearDown() {
        nodeEvent.destory(local.getId());
    }
}
