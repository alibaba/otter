package com.alibaba.otter.manager.biz.statistics.delay;

/**
 * @author jianghang 2011-11-21 下午03:07:35
 * @version 4.0.0
 */
public interface DelayCounter {

    public Long incAndGet(Long pipelineId, Long number);

    public Long decAndGet(Long pipelineId, Long number);

    public Long setAndGet(Long pipelineId, Long number);
}
