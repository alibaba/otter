package com.alibaba.otter.shared.communication.model.arbitrate;

import com.alibaba.otter.shared.communication.core.model.Event;

public class NodeAlarmEvent extends Event {

    private static final long serialVersionUID = 476657754177940448L;

    private Long              nid;                                   // 发送报警的node机器id
    private Long              pipelineId;                            // 对应出错的pipelineId
    private String            title;
    private String            message;

    public NodeAlarmEvent(){
        super(ArbitrateEventType.nodeAlarm);
    }

    public Long getNid() {
        return nid;
    }

    public void setNid(Long nid) {
        this.nid = nid;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
