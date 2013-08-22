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

package com.alibaba.otter.node.etl.load.loader.db;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.node.etl.load.loader.db.context.DbLoadContext;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;

/**
 * dump记录
 * 
 * @author jianghang 2011-11-9 下午03:52:26
 * @version 4.0.0
 */
public class DbLoadDumper {

    private static final String SEP                    = SystemUtils.LINE_SEPARATOR;

    private static String       context_format         = null;
    private static String       eventData_format       = null;
    private static int          event_default_capacity = 1024;                      // 预设值StringBuilder，减少扩容影响

    static {
        context_format = SEP + "****************************************************" + SEP;
        context_format += "* Identity : {0} *" + SEP;
        context_format += "* total Data : [{1}] , success Data : [{2}] , failed Data : [{3}] , Interrupt : [{4}]" + SEP;
        context_format += "****************************************************" + SEP;
        context_format += "* process Data  *" + SEP;
        context_format += "{5}" + SEP;
        context_format += "****************************************************" + SEP;
        context_format += "* failed Data *" + SEP;
        context_format += "{6}" + SEP;
        context_format += "****************************************************" + SEP;

        eventData_format = "-----------------" + SEP;
        eventData_format += "- PairId: {0} , TableId: {1} , EventType : {2} , Time : {3} " + SEP;
        eventData_format += "- Consistency : {4} , Mode : {5} " + SEP;
        eventData_format += "-----------------" + SEP;
        eventData_format += "---Pks" + SEP;
        eventData_format += "{6}" + SEP;
        eventData_format += "---oldPks" + SEP;
        eventData_format += "{7}" + SEP;
        eventData_format += "---Columns" + SEP;
        eventData_format += "{8}" + SEP;
        eventData_format += "---Sql" + SEP;
        eventData_format += "{9}" + SEP;
    }

    public static String dumpContext(DbLoadContext context) {
        int successed = context.getProcessedDatas().size();
        int failed = context.getFailedDatas().size();
        int all = context.getPrepareDatas().size();
        boolean isInterrupt = (all != (failed + successed));
        return MessageFormat.format(context_format, context.getIdentity().toString(), all, successed, failed,
                                    isInterrupt, dumpEventDatas(context.getProcessedDatas()),
                                    dumpEventDatas(context.getFailedDatas()));
    }

    public static String dumpEventDatas(List<EventData> eventDatas) {
        if (CollectionUtils.isEmpty(eventDatas)) {
            return StringUtils.EMPTY;
        }

        // 预先设定容量大小
        StringBuilder builder = new StringBuilder(event_default_capacity * eventDatas.size());
        for (EventData data : eventDatas) {
            builder.append(dumpEventData(data));
        }
        return builder.toString();
    }

    public static String dumpEventData(EventData eventData) {
        String type = (eventData.getEventType() != null) ? eventData.getEventType().getValue() : "";
        String consistency = (eventData.getSyncConsistency() != null) ? eventData.getSyncConsistency().getValue() : "";
        String mode = (eventData.getSyncMode() != null) ? eventData.getSyncMode().getValue() : "";
        return MessageFormat.format(eventData_format, eventData.getPairId(), eventData.getTableId(), type,
                                    String.valueOf(eventData.getExecuteTime()), consistency, mode,
                                    dumpEventColumn(eventData.getKeys()), dumpEventColumn(eventData.getOldKeys()),
                                    dumpEventColumn(eventData.getColumns()), "\t" + eventData.getSql());
    }

    private static String dumpEventColumn(List<EventColumn> columns) {
        StringBuilder builder = new StringBuilder(event_default_capacity);
        int size = columns.size();
        for (int i = 0; i < size; i++) {
            EventColumn column = columns.get(i);
            builder.append("\t").append(column.toString());
            if (i < columns.size() - 1) {
                builder.append(SEP);
            }
        }
        return builder.toString();
    }
}
