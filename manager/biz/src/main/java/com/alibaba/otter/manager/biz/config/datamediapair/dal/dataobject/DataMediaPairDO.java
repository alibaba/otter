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

package com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.otter.shared.common.model.config.data.ColumnPairMode;

/**
 * @author simon
 */
public class DataMediaPairDO implements Serializable {

    private static final long serialVersionUID = -7771432925148858183L;
    private Long              id;
    private Long              sourceDataMediaId;
    private Long              targetDataMediaId;
    private Long              pullWeight;                              // 介质A中获取数据的权重
    private Long              pushWeight;                              // 介质B中写入数据的权重
    private String            resolver;                                // 关联数据解析类
    private String            filter;                                  // 数据过滤处理类
    private ColumnPairMode    columnPairMode;
    private Long              pipelineId;                              // 同步任务id
    private Date              gmtCreate;
    private Date              gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceDataMediaId() {
        return sourceDataMediaId;
    }

    public void setSourceDataMediaId(Long sourceDataMediaId) {
        this.sourceDataMediaId = sourceDataMediaId;
    }

    public Long getTargetDataMediaId() {
        return targetDataMediaId;
    }

    public void setTargetDataMediaId(Long targetDataMediaId) {
        this.targetDataMediaId = targetDataMediaId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
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

    public void setPushWeight(Long pushWeight) {
        this.pushWeight = pushWeight;
    }

    public String getResolver() {
        return resolver;
    }

    public void setResolver(String resolver) {
        this.resolver = resolver;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public ColumnPairMode getColumnPairMode() {
        return columnPairMode;
    }

    public void setColumnPairMode(ColumnPairMode columnPairMode) {
        this.columnPairMode = columnPairMode;
    }

}
