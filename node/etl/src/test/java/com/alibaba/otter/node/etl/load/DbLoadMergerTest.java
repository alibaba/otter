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

package com.alibaba.otter.node.etl.load;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.BaseDbTest;
import com.alibaba.otter.node.etl.load.loader.db.DbLoadMerger;
import com.alibaba.otter.node.etl.load.loader.db.DbLoadMerger.RowKey;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.google.common.collect.MapMaker;

public class DbLoadMergerTest extends BaseDbTest {

    private static final int      COLUMN_TYPE    = 1;

    private static final long     TABLE_ID       = 10;

    private static final String   SCHEMA_NAME    = "test";

    private static final String   TABLE_NAME     = "test";

    private static final String   KEY_NAME       = "id";

    private static final String   KEY_VALUE      = "100";

    private static final String   KEY_VALUE_NEW1 = "1001";
    private static final String   KEY_VALUE_NEW2 = "1002";
    private static final String   KEY_VALUE_NEW3 = "1003";
    private static final String   KEY_VALUE_NEW4 = "1004";

    private static final String[] COLUMN_NAMES1  = { "name", "password" };

    private static final String[] COLUMN_NAMES2  = { "name", "age" };

    /**
     * 测试insert+update
     */
    @Test
    public void testMergeWithSameKeyOfIU() {
        Map<RowKey, EventData> mergeMap = new MapMaker().makeMap();
        DbLoadMerger.merge(makeInsertEventData(), mergeMap);
        DbLoadMerger.merge(makeUpdateEventData(), mergeMap);

        for (Entry<RowKey, EventData> entry : mergeMap.entrySet()) {
            RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeys().get(0);
            Assert.assertEquals(KEY_VALUE, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            EventData eventData = entry.getValue();

            Assert.assertEquals(SCHEMA_NAME, eventData.getSchemaName());
            Assert.assertEquals(TABLE_NAME, eventData.getTableName());
            Assert.assertEquals(TABLE_ID, eventData.getTableId());
            Assert.assertEquals(EventType.INSERT, eventData.getEventType());
            Assert.assertEquals(eventData.getOldKeys().size(), 0); // 不存在oldKeys

            List<EventColumn> columns = eventData.getColumns();
            Assert.assertEquals(3, columns.size());
        }
    }

    /**
     * 测试insert+update+delete
     */
    @Test
    public void testMergeWithSameKeyOfIUD() {
        Map<RowKey, EventData> mergeMap = new MapMaker().makeMap();
        DbLoadMerger.merge(makeInsertEventData(), mergeMap);
        DbLoadMerger.merge(makeUpdateEventData(), mergeMap);
        DbLoadMerger.merge(makeDeleteEventData(), mergeMap);

        for (Entry<RowKey, EventData> entry : mergeMap.entrySet()) {
            RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeys().get(0);
            Assert.assertEquals(KEY_VALUE, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            EventData eventData = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, eventData.getSchemaName());
            Assert.assertEquals(TABLE_NAME, eventData.getTableName());
            Assert.assertEquals(TABLE_ID, eventData.getTableId());
            Assert.assertEquals(EventType.DELETE, eventData.getEventType());
            Assert.assertEquals(eventData.getOldKeys().size(), 0); // 不存在oldKeys

            List<EventColumn> columns = eventData.getColumns();
            Assert.assertEquals(0, columns.size());
        }
    }

    /**
     * 测试insert+update+delete+insert
     */
    @Test
    public void testMergeWithSameKeyOfIUDI() {
        Map<RowKey, EventData> mergeMap = new MapMaker().makeMap();
        DbLoadMerger.merge(makeInsertEventData(), mergeMap);
        DbLoadMerger.merge(makeUpdateEventData(), mergeMap);
        DbLoadMerger.merge(makeDeleteEventData(), mergeMap);
        DbLoadMerger.merge(makeInsertEventData(), mergeMap);

        for (Entry<RowKey, EventData> entry : mergeMap.entrySet()) {
            RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeys().get(0);
            Assert.assertEquals(KEY_VALUE, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            EventData eventData = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, eventData.getSchemaName());
            Assert.assertEquals(TABLE_NAME, eventData.getTableName());
            Assert.assertEquals(TABLE_ID, eventData.getTableId());
            Assert.assertEquals(EventType.INSERT, eventData.getEventType());
            Assert.assertEquals(eventData.getOldKeys().size(), 0); // 不存在oldKeys

            List<EventColumn> columns = eventData.getColumns();
            Assert.assertEquals(2, columns.size());
        }
    }

    /**
     * 测试在主键发生变化后的merge操作，Update/Update
     */
    @Test
    public void testMergeWithUpdateKeyOfUU() {
        Map<RowKey, EventData> mergeMap = new MapMaker().makeMap();
        DbLoadMerger.merge(makeUpdateEventData(KEY_VALUE, KEY_VALUE_NEW1), mergeMap);
        DbLoadMerger.merge(makeUpdateEventData(KEY_VALUE_NEW1, KEY_VALUE_NEW2), mergeMap);

        for (Entry<RowKey, EventData> entry : mergeMap.entrySet()) {
            RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeys().get(0);
            Assert.assertEquals(KEY_VALUE_NEW2, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            EventData eventData = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, eventData.getSchemaName());
            Assert.assertEquals(TABLE_NAME, eventData.getTableName());
            Assert.assertEquals(TABLE_ID, eventData.getTableId());
            Assert.assertEquals(EventType.UPDATE, eventData.getEventType());

            List<EventColumn> oldKeys = eventData.getOldKeys();
            List<EventColumn> keys = eventData.getKeys();
            Assert.assertNotSame(oldKeys, keys);
        }
    }

    /**
     * 测试在主键发生变化后的merge操作，Update/Update/delete
     */
    @Test
    public void testMergeWithUpdateKeyOfUUD() {
        Map<RowKey, EventData> mergeMap = new MapMaker().makeMap();
        DbLoadMerger.merge(makeUpdateEventData(KEY_VALUE_NEW1, KEY_VALUE_NEW2), mergeMap);
        DbLoadMerger.merge(makeUpdateEventData(KEY_VALUE_NEW2, KEY_VALUE), mergeMap);
        DbLoadMerger.merge(makeDeleteEventData(), mergeMap);

        for (Entry<RowKey, EventData> entry : mergeMap.entrySet()) {
            RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeys().get(0);
            Assert.assertEquals(KEY_VALUE_NEW1, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            EventData eventData = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, eventData.getSchemaName());
            Assert.assertEquals(TABLE_NAME, eventData.getTableName());
            Assert.assertEquals(TABLE_ID, eventData.getTableId());
            Assert.assertEquals(EventType.DELETE, eventData.getEventType());
            Assert.assertEquals(eventData.getOldKeys().size(), 0); // 不存在oldKeys
        }
    }

    /**
     * 测试在主键发生变化后的merge操作，Insert/Update/Update/Update/Update
     */
    @Test
    public void testMergeWithUpdateKeyOfIUUUU() {
        Map<RowKey, EventData> mergeMap = new MapMaker().makeMap();
        DbLoadMerger.merge(makeInsertEventData(), mergeMap);
        DbLoadMerger.merge(makeUpdateEventData(KEY_VALUE, KEY_VALUE_NEW1), mergeMap);
        DbLoadMerger.merge(makeUpdateEventData(KEY_VALUE_NEW1, KEY_VALUE_NEW2), mergeMap);
        DbLoadMerger.merge(makeUpdateEventData(KEY_VALUE_NEW2, KEY_VALUE_NEW3), mergeMap);
        DbLoadMerger.merge(makeUpdateEventData(KEY_VALUE_NEW3, KEY_VALUE_NEW4), mergeMap);

        for (Entry<RowKey, EventData> entry : mergeMap.entrySet()) {
            RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeys().get(0);
            Assert.assertEquals(KEY_VALUE_NEW4, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            EventData eventData = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, eventData.getSchemaName());
            Assert.assertEquals(TABLE_NAME, eventData.getTableName());
            Assert.assertEquals(TABLE_ID, eventData.getTableId());
            Assert.assertEquals(EventType.INSERT, eventData.getEventType());
            Assert.assertEquals(eventData.getOldKeys().size(), 0); // 不存在oldKeys
        }
    }

    /**
     * 测试在主键发生变化后的merge操作，Update/Update/Insert
     */
    @Test
    public void testMergeWithUpdateKeyOfUI() {
        Map<RowKey, EventData> mergeMap = new MapMaker().makeMap();
        DbLoadMerger.merge(makeUpdateEventData(KEY_VALUE_NEW1, KEY_VALUE_NEW2), mergeMap);
        DbLoadMerger.merge(makeUpdateEventData(KEY_VALUE_NEW2, KEY_VALUE), mergeMap);
        DbLoadMerger.merge(makeInsertEventData(), mergeMap);

        for (Entry<RowKey, EventData> entry : mergeMap.entrySet()) {
            RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeys().get(0);
            Assert.assertEquals(KEY_VALUE, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            EventData eventData = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, eventData.getSchemaName());
            Assert.assertEquals(TABLE_NAME, eventData.getTableName());
            Assert.assertEquals(TABLE_ID, eventData.getTableId());
            Assert.assertEquals(EventType.INSERT, eventData.getEventType());

            List<EventColumn> oldKeys = eventData.getOldKeys();
            List<EventColumn> keys = eventData.getKeys();

            Assert.assertNotSame(oldKeys, keys);
        }
    }

    /**
     * 测试在主键发生变化后的merge操作，Insert/Insert
     */
    @Test
    public void testMergeWithUpdateKeyOfII() {
        Map<RowKey, EventData> mergeMap = new MapMaker().makeMap();
        DbLoadMerger.merge(makeInsertEventData(), mergeMap);
        DbLoadMerger.merge(makeInsertEventData(), mergeMap);

        for (Entry<RowKey, EventData> entry : mergeMap.entrySet()) {
            RowKey key = entry.getKey();
            EventColumn keyColumn = key.getKeys().get(0);
            Assert.assertEquals(KEY_VALUE, keyColumn.getColumnValue());
            Assert.assertEquals(KEY_NAME, keyColumn.getColumnName());

            EventData eventData = entry.getValue();
            Assert.assertEquals(SCHEMA_NAME, eventData.getSchemaName());
            Assert.assertEquals(TABLE_NAME, eventData.getTableName());
            Assert.assertEquals(TABLE_ID, eventData.getTableId());
            Assert.assertEquals(EventType.INSERT, eventData.getEventType());

            List<EventColumn> oldKeys = eventData.getOldKeys();
            List<EventColumn> keys = eventData.getKeys();

            Assert.assertNotSame(oldKeys, keys);
        }
    }

    private EventData makeInsertEventData() {
        EventData eventData = new EventData();
        eventData.setEventType(EventType.INSERT);
        eventData.setSchemaName(SCHEMA_NAME);
        eventData.setTableName(TABLE_NAME);
        eventData.setTableId(TABLE_ID);

        List<EventColumn> keys = new ArrayList<EventColumn>();
        keys.add(makeEventColumn(KEY_NAME, KEY_VALUE, true));
        eventData.setKeys(keys);

        List<EventColumn> columns = new ArrayList<EventColumn>();
        int i = 0;
        for (String columnName : COLUMN_NAMES1) {
            columns.add(makeEventColumn(columnName, columnName + i, false));
        }
        eventData.setColumns(columns);
        return eventData;
    }

    private EventData makeUpdateEventData() {
        EventData eventData = new EventData();
        eventData.setEventType(EventType.UPDATE);
        eventData.setSchemaName(SCHEMA_NAME);
        eventData.setTableName(TABLE_NAME);
        eventData.setTableId(TABLE_ID);

        List<EventColumn> keys = new ArrayList<EventColumn>();
        keys.add(makeEventColumn(KEY_NAME, KEY_VALUE, true));
        eventData.setKeys(keys);

        List<EventColumn> columns = new ArrayList<EventColumn>();
        int i = 0;
        for (String columnName : COLUMN_NAMES2) {
            columns.add(makeEventColumn(columnName, columnName + i, false));
            i++;
        }
        eventData.setColumns(columns);
        return eventData;
    }

    private EventData makeUpdateEventData(String oldKeyValue, String newKeyValue) {
        EventData eventData = new EventData();
        eventData.setEventType(EventType.UPDATE);
        eventData.setSchemaName(SCHEMA_NAME);
        eventData.setTableName(TABLE_NAME);
        eventData.setTableId(TABLE_ID);

        List<EventColumn> oldKeys = new ArrayList<EventColumn>();
        oldKeys.add(makeEventColumn(KEY_NAME, oldKeyValue, true));
        List<EventColumn> newKeys = new ArrayList<EventColumn>();
        newKeys.add(makeEventColumn(KEY_NAME, newKeyValue, true));
        eventData.setKeys(newKeys);
        eventData.setOldKeys(oldKeys);
        return eventData;
    }

    private EventData makeDeleteEventData() {
        EventData eventData = new EventData();
        eventData.setEventType(EventType.DELETE);
        eventData.setSchemaName(SCHEMA_NAME);
        eventData.setTableName(TABLE_NAME);
        eventData.setTableId(TABLE_ID);

        List<EventColumn> keys = new ArrayList<EventColumn>();
        keys.add(makeEventColumn(KEY_NAME, KEY_VALUE, true));
        eventData.setKeys(keys);
        return eventData;
    }

    private EventColumn makeEventColumn(String columnName, String columnValue, boolean key) {
        EventColumn eventColumn = new EventColumn();
        eventColumn.setColumnName(columnName);
        eventColumn.setColumnType(COLUMN_TYPE);
        eventColumn.setColumnValue(columnValue);
        eventColumn.setKey(key);
        return eventColumn;
    }
}
