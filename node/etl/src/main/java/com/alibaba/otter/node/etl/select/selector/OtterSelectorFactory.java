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

package com.alibaba.otter.node.etl.select.selector;

import com.alibaba.otter.node.etl.OtterContextLocator;
import com.alibaba.otter.node.etl.select.selector.canal.CanalEmbedSelector;

/**
 * 获取对应的selector
 * 
 * @author jianghang 2012-8-1 上午10:25:06
 * @version 4.1.0
 */
public class OtterSelectorFactory {

    public OtterSelector getSelector(Long pipelineId) {
        CanalEmbedSelector selector = new CanalEmbedSelector(pipelineId);
        OtterContextLocator.autowire(selector);
        return selector;
    }

}
