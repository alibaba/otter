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
 * 每条变更数据关联的文件信息.
 * 
 * @author xiaoqing.zhouxq 2011-8-9 下午04:52:10
 */
public class FileData implements ObjectData, Serializable {

    private static final long serialVersionUID = -8758520351171874888L;

    /**
     * Aranda use.
     */
    private String            nameSpace;

    private String            path;

    private long              size;

    private long              lastModifiedTime;

    private long              tableId;

    private long              pairId;

    private EventType         eventType;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public long getPairId() {
        return pairId;
    }

    public void setPairId(long pairId) {
        this.pairId = pairId;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nameSpace == null) ? 0 : nameSpace.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FileData)) {
            return false;
        }
        FileData other = (FileData) obj;
        if (nameSpace == null) {
            if (other.nameSpace != null) {
                return false;
            }
        } else if (!nameSpace.equals(other.nameSpace)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
