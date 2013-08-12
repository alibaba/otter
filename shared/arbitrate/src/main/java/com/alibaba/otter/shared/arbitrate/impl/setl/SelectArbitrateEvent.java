package com.alibaba.otter.shared.arbitrate.impl.setl;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;

/**
 * 抽象select模块的调度接口
 * 
 * @author jianghang 2012-9-27 下午09:54:53
 * @version 4.1.0
 */
public interface SelectArbitrateEvent extends ArbitrateEvent {

    public EtlEventData await(Long pipelineId) throws InterruptedException;

    public void single(EtlEventData data);
}
