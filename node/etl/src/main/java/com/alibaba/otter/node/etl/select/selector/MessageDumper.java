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

package com.alibaba.otter.node.etl.select.selector;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;

/**
 * dump记录
 * 
 * @author jianghang 2011-11-9 下午03:52:26
 * @version 4.0.0
 */
public class MessageDumper {

    private static final String SEP                    = SystemUtils.LINE_SEPARATOR;
    private static final String TIMESTAMP_FORMAT       = "yyyy-MM-dd HH:mm:ss:SSS";
    private static String       context_format         = null;
    private static String       eventData_format       = null;
    private static int          event_default_capacity = 1024;                      // 预设值StringBuilder，减少扩容影响

    static {
        context_format = "* Batch Id: [{0}] ,total : [{1}] , normal : [{2}] , filter :[{3}] , Time : {4}" + SEP;
        context_format += "* Start : [{5}] " + SEP;
        context_format += "* End : [{6}] " + SEP;

        eventData_format = "-----------------" + SEP;
        eventData_format += "- TableId: {0} , Schema: {1} , Table: {2} " + SEP;
        eventData_format += "- Type: {3}  , ExecuteTime: {4} , Remedy: {5}" + SEP;
        eventData_format += "-----------------" + SEP;
        eventData_format += "---START" + SEP;
        eventData_format += "---Pks" + SEP;
        eventData_format += "{6}" + SEP;
        eventData_format += "---oldPks" + SEP;
        eventData_format += "{7}" + SEP;
        eventData_format += "---Columns" + SEP;
        eventData_format += "{8}" + SEP;
        eventData_format += "---END" + SEP;

    }

    public static String dumpMessageInfo(Message<EventData> message, String startPosition, String endPosition, int total) {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP_FORMAT);
        int normal = message.getDatas().size();
        return MessageFormat.format(context_format, String.valueOf(message.getId()), total, normal, total - normal,
                                    format.format(now), startPosition, endPosition);
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
        boolean remedy = eventData.isRemedy();
        return MessageFormat.format(eventData_format, String.valueOf(eventData.getTableId()),
                                    eventData.getSchemaName(), eventData.getTableName(),
                                    eventData.getEventType().getValue(), String.valueOf(eventData.getExecuteTime()),
                                    remedy, dumpEventColumn(eventData.getKeys()),
                                    dumpEventColumn(eventData.getOldKeys()), dumpEventColumn(eventData.getColumns()),
                                    "\t" + eventData.getSql());
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
