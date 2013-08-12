package com.alibaba.otter.shared.arbitrate;

import java.util.List;

import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.arbitrate.model.PositionEventData;
import com.alibaba.otter.shared.common.model.statistics.stage.ProcessStat;

/**
 * 仲裁器状态视图服务,允许查看当前的一些process/termin状态信息
 * 
 * @author jianghang 2011-9-27 下午05:20:42
 * @version 4.0.0
 */
public interface ArbitrateViewService {

    /**
     * 查询当前的mainstem工作信息
     */
    MainStemEventData mainstemData(Long channelId, Long pipelineId);

    /**
     * 查询当前的process列表
     */
    List<ProcessStat> listProcesses(Long channelId, Long pipelineId);

    /**
     * 查询下一个processId
     */
    Long getNextProcessId(Long channelId, Long pipelineId);

    /**
     * 获取canal cursor
     */
    PositionEventData getCanalCursor(String destination, short clientId);

    /**
     * 删除canal cursor
     */
    void removeCanalCursor(String destination, short clientId);
}
