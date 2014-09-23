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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.manager.biz.config.record.LogRecordService;
import com.alibaba.otter.shared.common.model.config.record.LogRecord;

/**
 * 类NodeList.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-10-25 上午10:25:27
 */
public class LogRecordList {

    @Resource(name = "logRecordService")
    private LogRecordService logRecordService;

    public void execute(@Param("pageIndex") int pageIndex, @Param("pipelineId") String pipelineId,
                        @Param("monitorName") String monitorName, @Param("searchKey") String searchKey, Context context)
                                                                                                                        throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> condition = new HashMap<String, Object>();
        if ("请输入关键字(目前支持CID,PID搜索)".equals(searchKey)) {
            searchKey = "";
        }
        condition.put("searchKey", searchKey);
        condition.put("monitorName", monitorName);
        condition.put("pipelineId", pipelineId);

        int count = logRecordService.getCount(condition);
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());

        List<LogRecord> logRecords = logRecordService.listByCondition(condition);
        // List<LogRecord> logRecords = logRecordService.listAll();

        context.put("logRecords", logRecords);
        context.put("paginator", paginator);
        context.put("searchKey", searchKey);
        context.put("pipelineId", pipelineId);
        context.put("monitorName", monitorName);
    }
}
