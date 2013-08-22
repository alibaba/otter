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

package com.alibaba.otter.manager.web.home.module.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.record.LogRecordService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.record.LogRecord;

public class LogRecordTab {

    @Resource(name = "logRecordService")
    private LogRecordService logRecordService;
    @Resource(name = "channelService")
    private ChannelService   channelService;

    public void execute(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey,
                        @Param("pipelineId") Long pipelineId, Context context) throws Exception {

        @SuppressWarnings("unchecked")
        Map<String, Object> condition = new HashMap<String, Object>();
        if ("请输入关键字(目前支持log内容关键字搜索)".equals(searchKey)) {
            searchKey = "";
        }

        condition.put("pipelineId", pipelineId);
        condition.put("searchKey", searchKey);

        int count = logRecordService.getCount(condition);
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());

        List<LogRecord> logRecords = logRecordService.listByCondition(condition);
        for (LogRecord logRecord : logRecords) {
            if (!StringUtils.isEmpty(logRecord.getMessage())) {
                logRecord.setMessage(logRecord.getMessage().replaceAll("\n\t", "<br/>"));
            }
        }

        context.put("logRecords", logRecords);
        context.put("paginator", paginator);
        context.put("searchKey", searchKey);
        context.put("pipelineId", pipelineId);
        Channel channel = channelService.findByPipelineId(pipelineId);
        context.put("channel", channel);
    }
}
