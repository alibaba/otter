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

package com.alibaba.otter.shared.common.model.config.channel;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * channel数据对象
 * 
 * @author jianghang 2011-8-31 下午07:35:29
 */
public class Channel implements Serializable {

    private static final long serialVersionUID = 2345662422309356370L;
    private Long              id;                                       // 唯一标示id
    private String            name;
    private ChannelStatus     status;
    private String            description;                              // 描述信息
    private List<Pipeline>    pipelines;
    private Date              gmtCreate;
    private Date              gmtModified;
    private ChannelParameter  parameters       = new ChannelParameter();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Pipeline> getPipelines() {
        return pipelines;
    }

    public void setPipelines(List<Pipeline> pipelines) {
        this.pipelines = pipelines;
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

    public ChannelStatus getStatus() {
        return status;
    }

    public void setStatus(ChannelStatus status) {
        this.status = status;
    }

    public ChannelParameter getParameters() {
        return parameters;
    }

    public void setParameters(ChannelParameter parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }

}
