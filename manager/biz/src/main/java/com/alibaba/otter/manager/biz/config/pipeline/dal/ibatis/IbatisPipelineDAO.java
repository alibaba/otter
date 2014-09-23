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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.manager.biz.config.pipeline.dal.PipelineDAO;
import com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO;
import com.alibaba.otter.shared.common.utils.Assert;

/**
 * Pipeline的DAO层，ibatis的实现，主要是CRUD操作。
 * 
 * @author simon
 */
public class IbatisPipelineDAO extends SqlMapClientDaoSupport implements PipelineDAO {

    public PipelineDO insert(PipelineDO pipelineDo) {
        Assert.assertNotNull(pipelineDo);
        getSqlMapClientTemplate().insert("insertPipeline", pipelineDo);
        return pipelineDo;
    }

    public void delete(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        getSqlMapClientTemplate().delete("deletePipelineById", pipelineId);
    }

    public void update(PipelineDO pipelineDO) {
        Assert.assertNotNull(pipelineDO);
        getSqlMapClientTemplate().update("updatePipeline", pipelineDO);
    }

    public boolean checkUnique(PipelineDO pipelineDO) {
        int count = (Integer) getSqlMapClientTemplate().queryForObject("checkPipelineUnique", pipelineDO);
        return count == 0 ? true : false;
    }

    public PipelineDO findById(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        return (PipelineDO) getSqlMapClientTemplate().queryForObject("findPipelineById", pipelineId);
    }

    public List<PipelineDO> listByChannelIds(Long... channelId) {
        Assert.assertNotNull(channelId);
        return (List<PipelineDO>) getSqlMapClientTemplate().queryForList("listPipelinesByChannelIds", channelId);
    }

    public List<PipelineDO> listByCondition(Map condition) {
        List<PipelineDO> pipelineDos = getSqlMapClientTemplate().queryForList("listPipelines", condition);
        return pipelineDos;
    }

    public List<PipelineDO> listAll() {
        List<PipelineDO> pipelines = getSqlMapClientTemplate().queryForList("listPipelines");
        return pipelines;
    }

    public List<PipelineDO> listByMultiId(Long... identities) {
        List<PipelineDO> pipelineDos = getSqlMapClientTemplate().queryForList("listPipelineByIds", identities);
        return pipelineDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getPipelineCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getPipelineCount", condition);
        return count.intValue();
    }

    public List<PipelineDO> listByDestinationCondition(String canalName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("searchKey", canalName);
        List<PipelineDO> pipelineDos = getSqlMapClientTemplate().queryForList("listByDestinationCondition", map);
        return pipelineDos;
    }

}
