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

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * @author xiaoqing.zhouxq 2011-8-10 上午10:42:27
 */
public class EventColumn implements Serializable {

    private static final long serialVersionUID = 8881024631437131042L;

    private int               index;

    private int               columnType;

    private String            columnName;

    /**
     * timestamp,Datetime是一个long型的数字.
     */
    private String            columnValue;

    private boolean           isNull;

    private boolean           isKey;

    /**
     * 2012.08.09 add by ljh , 新加字段，用于表明是否为真实变更字段，只针对非主键字段有效<br>
     * 因为FileResolver/EventProcessor会需要所有字段数据做分析，但又想保留按需字段同步模式
     * 
     * <pre>
     * 可以简单理解isUpdate代表是否需要在目标库执行数据变更，针对update有效，默认insert/delete为true
     * 1. row模式，所有字段均为updated
     * 2. field模式，通过db反查得到的结果，均为updated
     * 3. 其余场景，根据判断是否变更过，设置updated数据
     * </pre>
     */
    private boolean           isUpdate         = true;

    public int getColumnType() {
        return columnType;
    }

    public void setColumnType(int columnType) {
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnValue() {
        if (isNull) {
            // 如果为null值，强制设置为null, canal主要是走protobuf协议，String值默认为空字符，无法标示为null对象
            columnValue = null;
            return null;
        } else {
            return columnValue;
        }
    }

    public void setColumnValue(String columnValue) {
        this.columnValue = columnValue;
    }

    public boolean isNull() {
        return isNull;
    }

    public void setNull(boolean isNull) {
        this.isNull = isNull;
    }

    public boolean isKey() {
        return isKey;
    }

    public void setKey(boolean isKey) {
        this.isKey = isKey;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    public EventColumn clone() {
        EventColumn column = new EventColumn();
        column.setIndex(index);
        column.setColumnName(columnName);
        column.setColumnType(columnType);
        column.setColumnValue(columnValue);
        column.setKey(isKey);
        column.setNull(isNull);
        column.setUpdate(isUpdate);
        return column;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
        result = prime * result + columnType;
        result = prime * result + ((columnValue == null) ? 0 : columnValue.hashCode());
        result = prime * result + index;
        result = prime * result + (isKey ? 1231 : 1237);
        result = prime * result + (isNull ? 1231 : 1237);
        result = prime * result + (isUpdate ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        EventColumn other = (EventColumn) obj;
        if (columnName == null) {
            if (other.columnName != null) return false;
        } else if (!columnName.equals(other.columnName)) return false;
        if (columnType != other.columnType) return false;
        if (columnValue == null) {
            if (other.columnValue != null) return false;
        } else if (!columnValue.equals(other.columnValue)) return false;
        if (index != other.index) return false;
        if (isKey != other.isKey) return false;
        if (isNull != other.isNull) return false;
        if (isUpdate != other.isUpdate) return false;
        return true;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }

}
