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

package com.alibaba.otter.shared.arbitrate.impl.setl.delegate;

import java.util.List;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter.ArbitrateMode;

/**
 * delegate一些共用的判断摸索
 * 
 * @author jianghang 2012-9-28 上午10:07:16
 * @version 4.1.0
 */
public class AbstractDelegateArbitrateEvent implements ArbitrateEvent {

    public ArbitrateMode chooseMode(Long pipelineId) {
        Pipeline pipeline = ArbitrateConfigUtils.getPipeline(pipelineId);
        ArbitrateMode arbitrateMode = pipeline.getParameters().getArbitrateMode();
        // 重新计算arbitrateMode
        ArbitrateMode result = null;
        switch (arbitrateMode) {
            case AUTOMATIC:
                // 1. 如果s/e/t/l都是由1台或者多台机器提供服务，则选择内存模式
                // 2. 如果s/e由一组机器，t/l由另一组机器提供服务，则选择rpc模式
                if (containAll(pipeline.getSelectNodes(), pipeline.getExtractNodes())
                    && containAll(pipeline.getSelectNodes(), pipeline.getLoadNodes())) {
                    result = ArbitrateMode.MEMORY;
                } else {
                    result = ArbitrateMode.RPC;
                }

                break;
            default:
                result = arbitrateMode;
                break;
        }

        return result;
    }

    /**
     * 判断srcNodes是否在targetNodes中都包含
     */
    private boolean containAll(List<Node> srcNodes, List<Node> targetNodes) {
        boolean result = true;
        for (Node node : srcNodes) {
            result &= targetNodes.contains(node);
        }

        return result;
    }

}
