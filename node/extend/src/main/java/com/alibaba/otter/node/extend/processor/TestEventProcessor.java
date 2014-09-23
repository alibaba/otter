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

package com.alibaba.otter.node.extend.processor;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;

public class TestEventProcessor extends AbstractEventProcessor {

    public boolean process(EventData eventData) {
        // 基本步骤：
        // 1. 获取binlog中的变更字段
        // 2. 根据业务逻辑进行判断，如果需要忽略本条数据同步，直接返回false，否则返回true
        // 3. 根据业务逻辑进行逻辑转化，比如可以修改整个EventData数据.  

        // 本文例子：源库的每条binlog变更，记录到一个日志表binlog
        // create table test.binlog(
        //        id bigint(20) auto_increment,
        //        oschema varchar(256),
        //        otable varchar(256),
        //        gtime varchar(32)
        //        ovalue text,
        //        primary key(id);
        //    )
        // 在process处理中，可以修改EventData的任何数据，达到数据转换的效果, just have fun.
        JSONObject col = new JSONObject();
        JSONArray array = new JSONArray();
        for (EventColumn column : eventData.getColumns()) {
            JSONObject obj = this.doColumn(column);
            array.add(obj);
        }

        for (EventColumn key : eventData.getKeys()) {
            JSONObject obj = this.doColumn(key);
            array.add(obj);
        }

        col.put("schema", eventData.getSchemaName());
        col.put("table", eventData.getTableName());
        col.put("columns", array);
        col.put("dml", eventData.getEventType());
        col.put("exectime", eventData.getExecuteTime());

        // 构造新的主键
        EventColumn id = new EventColumn();
        id.setColumnValue(eventData.getSchemaName());
        id.setColumnType(Types.BIGINT);
        id.setColumnName("id");
        // 构造新的字段
        EventColumn schema = new EventColumn();
        schema.setColumnValue(eventData.getSchemaName());
        schema.setColumnType(Types.VARCHAR);
        schema.setColumnName("oschema");

        EventColumn table = new EventColumn();
        table.setColumnValue(eventData.getTableName());
        table.setColumnType(Types.VARCHAR);
        table.setColumnName("otable");

        EventColumn ovalue = new EventColumn();
        ovalue.setColumnValue(col.toJSONString());
        ovalue.setColumnType(Types.VARCHAR);
        ovalue.setColumnName("ovalue");

        EventColumn gtime = new EventColumn();
        gtime.setColumnValue(eventData.getExecuteTime() + "");
        gtime.setColumnType(Types.VARCHAR);
        gtime.setColumnName("gtime");

        // 替换为新的字段和主键信息
        List<EventColumn> cols = new ArrayList<EventColumn>();
        cols.add(schema);
        cols.add(table);
        cols.add(gtime);
        cols.add(ovalue);
        eventData.setColumns(cols);

        List<EventColumn> keys = new ArrayList<EventColumn>();
        keys.add(id);
        eventData.setKeys(keys);

        //修改数据meta信息
        eventData.setEventType(EventType.INSERT);
        eventData.setSchemaName("test");
        eventData.setTableName("binlog");
        return true;
    }

    private JSONObject doColumn(EventColumn column) {
        JSONObject obj = new JSONObject();
        obj.put("name", column.getColumnName());
        obj.put("update", column.isUpdate());
        obj.put("key", column.isKey());
        if (column.getColumnType() != Types.BLOB && column.getColumnType() != Types.CLOB) {
            obj.put("value", column.getColumnValue());
        } else {
            obj.put("value", "");
        }
        return obj;
    }
}
