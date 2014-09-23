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

package com.alibaba.otter.manager.biz.config.node.dal.dataobject;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.otter.shared.common.model.config.node.NodeParameter;
import com.alibaba.otter.shared.common.model.config.node.NodeStatus;

/**
 * @author simon
 */
public class NodeDO implements Serializable {

    private static final long serialVersionUID = 9148286590254926037L;
    private Long              id;                                     // 唯一标示id
    private String            name;                                   // 机器名字
    private String            ip;                                     // 机器ip
    private Long              port;                                   // 和manager对应的通讯端口
    private NodeStatus        status;                                 // 对应状态
    private String            description;                            // 详细描述
    private NodeParameter     parameters;                             // node对应参数信息
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NodeParameter getParameters() {
        return parameters;
    }

    public void setParameters(NodeParameter parameters) {
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
