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

package com.alibaba.otter.shared.common.model.config.data;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * @author simon 2012-10-16 下午7:46:42
 * @version 4.1.0
 */
public class ExtensionData implements Serializable, Comparable<ExtensionData> {

    private static final long serialVersionUID = 5697237591471377596L;
    private ExtensionDataType extensionDataType;
    private String            clazzPath;
    private String            sourceText;
    private Long              timestamp;

    public ExtensionData(){
        this.timestamp = System.currentTimeMillis();
    }

    public ExtensionDataType getExtensionDataType() {
        return extensionDataType;
    }

    public void setExtensionDataType(ExtensionDataType extensionDataType) {
        this.extensionDataType = extensionDataType;
    }

    public String getClazzPath() {
        return clazzPath;
    }

    public void setClazzPath(String clazzPath) {
        this.clazzPath = clazzPath;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isBlank() {
        return !isNotBlank();
    }

    public boolean isNotBlank() {
        return (StringUtils.isNotBlank(clazzPath) || StringUtils.isNotBlank(sourceText));
    }

    @Override
    public int compareTo(ExtensionData o) {
        if (this.timestamp < o.getTimestamp()) {
            return -1;
        } else if (this.timestamp > o.getTimestamp()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clazzPath == null) ? 0 : clazzPath.hashCode());
        result = prime * result + ((extensionDataType == null) ? 0 : extensionDataType.hashCode());
        result = prime * result + ((sourceText == null) ? 0 : sourceText.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ExtensionData other = (ExtensionData) obj;
        if (clazzPath == null) {
            if (other.clazzPath != null) return false;
        } else if (!clazzPath.equals(other.clazzPath)) return false;
        if (extensionDataType != other.extensionDataType) return false;
        if (sourceText == null) {
            if (other.sourceText != null) return false;
        } else if (!sourceText.equals(other.sourceText)) return false;
        return true;
    }

}
