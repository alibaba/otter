package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.shared.etl.model.EventData;

/**
 * 测试下hint
 * 
 * @author jianghang 2014-6-11 下午4:20:32
 * @since 5.1.0
 */
public class HintEventProcessor extends AbstractEventProcessor {

    public boolean process(EventData eventData) {
        eventData.setHint("/* hint */");
        return true;
    }
}
