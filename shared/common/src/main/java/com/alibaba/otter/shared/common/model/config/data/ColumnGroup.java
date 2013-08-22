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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * 数据介质字段同步组
 * 
 * @author simon 2012-3-31 下午03:54:22
 */
public class ColumnGroup implements Serializable {

    private static final long serialVersionUID = 8903835374659632986L;
    private Long              id;
    private List<ColumnPair>  columnPairs      = new ArrayList<ColumnPair>();
    private Long              dataMediaPairId;
    private Date              gmtCreate;
    private Date              gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public List<ColumnPair> getColumnPairs() {
        return columnPairs;
    }

    public void setColumnPairs(List<ColumnPair> columnPairs) {
        this.columnPairs = columnPairs;
    }

    public Long getDataMediaPairId() {
        return dataMediaPairId;
    }

    public void setDataMediaPairId(Long dataMediaPairId) {
        this.dataMediaPairId = dataMediaPairId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
