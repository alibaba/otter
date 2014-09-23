/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    /**
     * 删除canal cursor + filter
     */
    void removeCanal(String destination, short clientId);

    /**
     * 删除canal meta信息
     */
    void removeCanal(String destination);
}
