package com.alibaba.otter.manager.biz.config.record;

import java.util.List;

import com.alibaba.otter.manager.biz.common.baseservice.GenericService;
import com.alibaba.otter.shared.common.model.config.record.LogRecord;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * @author simon 2012-6-15 下午1:49:17
 */
public interface LogRecordService extends GenericService<LogRecord> {

    public void create(Event event);

    public List<LogRecord> listByPipelineId(Long pipelineId);

    public List<LogRecord> listByPipelineIdWithoutContent(Long pipelineId);

}
