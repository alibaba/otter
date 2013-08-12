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
