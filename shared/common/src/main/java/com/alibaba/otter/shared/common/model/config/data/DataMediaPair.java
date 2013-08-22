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
 * 介质A -> 介质B 的同步映射关系对
 * 
 * @author jianghang 2011-9-2 上午11:41:33
 */
public class DataMediaPair implements Serializable {

    private static final long serialVersionUID = 6173105172139714032L;
    private Long              id;
    private Long              pipelineId;                                     // 同步任务id
    private DataMedia         source;
    private DataMedia         target;
    private Long              pullWeight;                                     // 介质A中获取数据的权重
    private Long              pushWeight;                                     // 介质B中写入数据的权重
    private ExtensionData     resolverData;                                   // 关联数据解析类
    private ExtensionData     filterData;                                     // filter解析类
    private ColumnPairMode    columnPairMode   = ColumnPairMode.INCLUDE;
    private List<ColumnPair>  columnPairs      = new ArrayList<ColumnPair>();
    private List<ColumnGroup> columnGroups     = new ArrayList<ColumnGroup>();
    private Date              gmtCreate;
    private Date              gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public DataMedia getSource() {
        return source;
    }

    public void setSource(DataMedia source) {
        this.source = source;
    }

    public DataMedia getTarget() {
        return target;
    }

    public void setTarget(DataMedia target) {
        this.target = target;
    }

    public Long getPullWeight() {
        return pullWeight;
    }

    public void setPullWeight(Long pullWeight) {
        this.pullWeight = pullWeight;
    }

    public Long getPushWeight() {
        return pushWeight;
    }

    public ExtensionData getResolverData() {
        return resolverData;
    }

    public void setResolverData(ExtensionData resolverData) {
        this.resolverData = resolverData;
    }

    public void setPushWeight(Long pushWeight) {
        this.pushWeight = pushWeight;
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

    public ExtensionData getFilterData() {
        return filterData;
    }

    public void setFilterData(ExtensionData filterData) {
        this.filterData = filterData;
    }

    public List<ColumnPair> getColumnPairs() {
        return columnPairs;
    }

    public void setColumnPairs(List<ColumnPair> columnPairs) {
        this.columnPairs = columnPairs;
    }

    public List<ColumnGroup> getColumnGroups() {
        return columnGroups;
    }

    public void setColumnGroups(List<ColumnGroup> columnGroups) {
        this.columnGroups = columnGroups;
    }

    public boolean isExistFilter() {
        return (filterData != null && filterData.isNotBlank());
    }

    public boolean isExistResolver() {
        return (resolverData != null && resolverData.isNotBlank());
    }

    public ColumnPairMode getColumnPairMode() {
        return columnPairMode == null ? ColumnPairMode.INCLUDE : columnPairMode;
    }

    public void setColumnPairMode(ColumnPairMode columnPairMode) {
        this.columnPairMode = columnPairMode;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }

}
