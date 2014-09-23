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

package com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject;

import java.io.Serializable;
import java.util.Date;

public class PipelineNodeRelationDO implements Serializable {

    private static final long serialVersionUID = -2066978336563209425L;
    private Long              id;
    private Long              nodeId;
    private Long              PipelineId;
    private Location          location;                                // 表示Node位于该pipeline的源或是目的
    private Date              gmtCreate;
    private Date              gmtModified;

    public static enum Location {
        SELECT, EXTRACT, LOAD;

        public boolean isSelect() {
            return this.equals(Location.SELECT);
        }

        public boolean isExtract() {
            return this.equals(Location.EXTRACT);
        }

        public boolean isLoad() {
            return this.equals(Location.LOAD);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Long getPipelineId() {
        return PipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        PipelineId = pipelineId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
