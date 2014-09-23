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

package com.alibaba.otter.manager.biz.config.canal.dal.dataobject;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.alibaba.otter.canal.instance.manager.model.CanalStatus;

/**
 * 类CanalDO.java的实现描述：TODO 类实现描述
 * 
 * @author sarah.lij 2012-7-25 下午05:11:18
 */
public class CanalDO implements Serializable {

    private static final long serialVersionUID = 9148286590254926037L;

    private Long              id;                                     // 唯一标示id
    private String            name;                                   // canal名字
    private String            description;                            // 描述
    private CanalStatus       status;
    private CanalParameter    parameters;                             // 参数定义
    private Date              gmtCreate;                              // 创建时间
    private Date              gmtModified;                            // 修改时间

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

    public CanalStatus getStatus() {
        return status;
    }

    public void setStatus(CanalStatus status) {
        this.status = status;
    }

    public CanalParameter getParameters() {
        return parameters;
    }

    public void setParameters(CanalParameter parameters) {
        this.parameters = parameters;
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

}
