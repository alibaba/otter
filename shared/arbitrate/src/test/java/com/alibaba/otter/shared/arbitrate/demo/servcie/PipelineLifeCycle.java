package com.alibaba.otter.shared.arbitrate.demo.servcie;

public interface PipelineLifeCycle {

    public void submit(Long pipelineId);

    public void destory(Long pipelineId);
}
