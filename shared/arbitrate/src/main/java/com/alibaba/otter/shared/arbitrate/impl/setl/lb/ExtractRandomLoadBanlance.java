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

package com.alibaba.otter.shared.arbitrate.impl.setl.lb;

import java.util.List;

import com.alibaba.otter.shared.common.model.config.node.Node;

/**
 * extract模块的负载均衡实现
 * 
 * @author jianghang 2011-9-20 下午01:24:22
 * @version 4.0.0
 */
public class ExtractRandomLoadBanlance extends RandomLoadBalance {

    public ExtractRandomLoadBanlance(Long pipelineId){
        super(pipelineId);
    }

    @Override
    public List<Node> getAliveNodes() {
        return getExtractAliveNodes();
    }

}
