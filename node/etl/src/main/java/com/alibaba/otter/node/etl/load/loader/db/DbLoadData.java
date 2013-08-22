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

import java.util.ArrayList;
import java.util.List;

import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;

/**
 * 将同一个weight下的EventData进行数据归类,按表和insert/update/delete类型进行分类
 * 
 * <pre>
 * 归类用途：对insert语句进行batch优化
 * 1. mysql索引的限制，需要避免insert并发执行
 * </pre>
 * 
 * @author jianghang 2011-11-9 下午04:28:35
 * @version 4.0.0
 */
public class DbLoadData {

    private List<TableLoadData> tables = new ArrayList<TableLoadData>();

    public DbLoadData(){
        // nothing
    }

    public DbLoadData(List<EventData> datas){
        for (EventData data : datas) {
            merge(data);
        }
    }

    public void merge(EventData data) {
        TableLoadData tableData = findTableData(data.getTableId());

        EventType type = data.getEventType();
        if (type.isInsert()) {
            tableData.getInsertDatas().add(data);
        } else if (type.isUpdate()) {
            tableData.getUpadateDatas().add(data);
        } else if (type.isDelete()) {
            tableData.getDeleteDatas().add(data);
        }
    }

    public List<TableLoadData> getTables() {
        return tables;
    }

    private synchronized TableLoadData findTableData(Long tableId) {
        for (TableLoadData table : tables) {
            if (table.getTableId().equals(tableId)) {
                return table;
            }
        }

        TableLoadData data = new TableLoadData(tableId);
        tables.add(data);
        return data;
    }

    /**
     * 按table进行分类
     */
    public static class TableLoadData {

        private Long            tableId;
        private List<EventData> insertDatas  = new ArrayList<EventData>();
        private List<EventData> upadateDatas = new ArrayList<EventData>();
        private List<EventData> deleteDatas  = new ArrayList<EventData>();

        public TableLoadData(Long tableId){
            this.tableId = tableId;
        }

        public List<EventData> getInsertDatas() {
            return insertDatas;
        }

        public void setInsertDatas(List<EventData> insertDatas) {
            this.insertDatas = insertDatas;
        }

        public List<EventData> getUpadateDatas() {
            return upadateDatas;
        }

        public void setUpadateDatas(List<EventData> upadateDatas) {
            this.upadateDatas = upadateDatas;
        }

        public List<EventData> getDeleteDatas() {
            return deleteDatas;
        }

        public void setDeleteDatas(List<EventData> deleteDatas) {
            this.deleteDatas = deleteDatas;
        }

        public Long getTableId() {
            return tableId;
        }

        public void setTableId(Long tableId) {
            this.tableId = tableId;
        }

    }
}
