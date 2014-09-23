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

package com.alibaba.otter.manager.biz.config.pipeline.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineNodeRelationDO;

/**
 * 考虑是否需要 类PipelineNodeRelationDAO.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-10-31 下午11:41:53
 */
public interface PipelineNodeRelationDAO extends GenericDAO<PipelineNodeRelationDO> {

    public void insertBatch(List<PipelineNodeRelationDO> pipelineNodeRelationDos);

    public void updateByNodeId(Long... nodeId);

    public void deleteByPipelineId(Long pipelineId);

    public void deleteByNodeId(Long... nodeId);

    public List<PipelineNodeRelationDO> listByPipelineIds(Long... pipelineId);

    public List<PipelineNodeRelationDO> listByNodeId(Long nodeId);
}
