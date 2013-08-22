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

package com.alibaba.otter.manager.biz.config.parameter.dal.dataobject;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;

/**
 * @author sarah.lij 2012-4-13 下午04:46:04
 */
public class SystemParameterDO implements Serializable {

    private static final long serialVersionUID = 9148286590254926037L;
    private Long              id;                                     // 唯一标示id
    private SystemParameter   value;                                  // 系统参数值
    private Date              gmtCreate;                              // 创建时间
    private Date              gmtModified;                            // 修改时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SystemParameter getValue() {
        return value;
    }

    public void setValue(SystemParameter value) {
        this.value = value;
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
