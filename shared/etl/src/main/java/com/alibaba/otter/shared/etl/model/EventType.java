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

package com.alibaba.otter.shared.etl.model;

/**
 * chang the eventtype num to I/U/D/C/A/E.
 * 
 * @author xiaoqing.zhouxq 2010-7-22 下午02:47:44
 */
public enum EventType {

    /**
     * Insert row.
     */
    INSERT("I"),

    /**
     * Update row.
     */
    UPDATE("U"),

    /**
     * Delete row.
     */
    DELETE("D"),

    /**
     * Create table.
     */
    CREATE("C"),

    /**
     * Alter table.
     */
    ALTER("A"),

    /**
     * Erase table.
     */
    ERASE("E"),

    /**
     * Query.
     */
    QUERY("Q");

    private String value;

    private EventType(String value){
        this.value = value;
    }

    public boolean isInsert() {
        return this.equals(EventType.INSERT);
    }

    public boolean isUpdate() {
        return this.equals(EventType.UPDATE);
    }

    public boolean isDelete() {
        return this.equals(EventType.DELETE);
    }

    public boolean isCreate() {
        return this.equals(EventType.CREATE);
    }

    public boolean isAlter() {
        return this.equals(EventType.ALTER);
    }

    public boolean isErase() {
        return this.equals(EventType.ERASE);
    }

    public boolean isQuery() {
        return this.equals(EventType.QUERY);
    }

    public static EventType valuesOf(String value) {
        EventType[] eventTypes = values();
        for (EventType eventType : eventTypes) {
            if (eventType.value.equalsIgnoreCase(value)) {
                return eventType;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
