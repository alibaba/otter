package com.alibaba.otter.shared.arbitrate.impl.setl;

import com.alibaba.otter.shared.arbitrate.model.TerminEventData;

public interface TerminArbitrateEvent {

    public TerminEventData await(Long pipelineId) throws InterruptedException;

    public void exhaust(Long pipelineId);

    public void ack(TerminEventData data);

    public int size(Long pipelineId);

    public void single(final TerminEventData data);

}
