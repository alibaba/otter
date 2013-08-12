package com.alibaba.otter.node.etl.extract;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.alibaba.otter.shared.common.model.config.enums.StageType;

/**
 * @author simon 2012-12-3 下午5:15:15
 * @version 4.1.0
 */
public class SetlFuture<V> extends FutureTask<V> {

    private StageType         stageType;
    private Long              processId;
    private Map<Long, Future> pendingFuture;

    public SetlFuture(StageType stageType, Long processId, Map<Long, Future> pendingFuture, Runnable runnable){
        super(runnable, null);
        this.stageType = stageType;
        this.pendingFuture = pendingFuture;
        this.pendingFuture.put(processId, this);
        this.processId = processId;
    }

    protected void done() {
        pendingFuture.remove(processId); // 完成了，将自己从pendingFuture移除
    }

    public String toString() {
        return "SetlFuture [processId=" + processId + ", stageType=" + stageType + "]";
    }

}
