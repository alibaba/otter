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

package com.alibaba.otter.manager.biz.config.pipeline.dal.ibatis;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.pipeline.dal.PipelineNodeRelationDAO;
import com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineNodeRelationDO;

/**
 * @author simon
 */
public class IbatisPipelineNodeRelationDAO extends SqlMapClientDaoSupport implements PipelineNodeRelationDAO {

    public PipelineNodeRelationDO insert(PipelineNodeRelationDO pipelineNodeRelationDo) {
        Assert.assertNotNull(pipelineNodeRelationDo);
        getSqlMapClientTemplate().insert("insertPipelineNodeRelation", pipelineNodeRelationDo);
        return pipelineNodeRelationDo;
    }

    public void insertBatch(List<PipelineNodeRelationDO> pipelineNodeRelationDos) {
        try {
            getSqlMapClientTemplate().getSqlMapClient().startBatch();

            Iterator it = pipelineNodeRelationDos.iterator();
            while (it.hasNext()) {
                PipelineNodeRelationDO pipelineNodeRelationDo = (PipelineNodeRelationDO) it.next();
                getSqlMapClientTemplate().getSqlMapClient().insert("insertPipelineNodeRelation", pipelineNodeRelationDo);
            }

            getSqlMapClientTemplate().getSqlMapClient().executeBatch();

        } catch (SQLException ex1) {
            ex1.printStackTrace();
        }

    }

    public void delete(Long pipelineNodeRelationId) {
        Assert.assertNotNull(pipelineNodeRelationId);
        getSqlMapClientTemplate().delete("deletePipelineNodeRelationById", pipelineNodeRelationId);
    }

    public void update(PipelineNodeRelationDO pipelineNodeRelationDo) {
        Assert.assertNotNull(pipelineNodeRelationDo);
        getSqlMapClientTemplate().update("updatePipelineNodeRelation", pipelineNodeRelationDo);
    }

    public List<PipelineNodeRelationDO> listByPipelineIds(Long... pipelineId) {
        Assert.assertNotNull(pipelineId);
        return (List<PipelineNodeRelationDO>) getSqlMapClientTemplate().queryForList("listRelationsByPipelineIds",
                                                                                     pipelineId);
    }

    public List<PipelineNodeRelationDO> listByNodeId(Long nodeId) {
        Assert.assertNotNull(nodeId);
        return (List<PipelineNodeRelationDO>) getSqlMapClientTemplate().queryForList("listRelationsByNodeId", nodeId);
    }

    public boolean checkUnique(PipelineNodeRelationDO entityObj) {
        // TODO Auto-generated method stub
        return false;
    }

    public List<PipelineNodeRelationDO> listByCondition(Map condition) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<PipelineNodeRelationDO> listAll() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<PipelineNodeRelationDO> listByMultiId(Long... identities) {
        // TODO Auto-generated method stub
        return null;
    }

    public PipelineNodeRelationDO findById(Long identity) {
        // TODO Auto-generated method stub
        return null;
    }

    public void updateByNodeId(Long... nodeId) {
        // TODO Auto-generated method stub

    }

    public void deleteByPipelineId(Long pipelineId) {
        getSqlMapClientTemplate().delete("deleteRelationByPipelineId", pipelineId);
    }

    public void deleteByNodeId(Long... nodeId) {
        // TODO Auto-generated method stub

    }

    public int getCount() {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getRelationCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        // TODO Auto-generated method stub
        return 0;
    }

}
