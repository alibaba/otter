package com.alibaba.otter.shared.arbitrate.model;

/**
 * 对应pipeline的event data
 * 
 * @author jianghang 2011-8-17 上午10:31:46
 */
public class PipelineEventData extends EventData {

    private static final long serialVersionUID = 4223623194547317751L;
    private Long              pipelineId;                             // 通道id

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

}
