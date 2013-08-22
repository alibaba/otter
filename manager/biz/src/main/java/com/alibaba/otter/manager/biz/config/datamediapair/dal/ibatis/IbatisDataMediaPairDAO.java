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

package com.alibaba.otter.manager.biz.config.datamediapair.dal.ibatis;

import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.datamediapair.dal.DataMediaPairDAO;
import com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;

/**
 * DataMediaPair的DAO层，ibatis的实现，主要是CRUD操作。
 * 
 * @author simon
 */
public class IbatisDataMediaPairDAO extends SqlMapClientDaoSupport implements DataMediaPairDAO {

    public DataMediaPairDO insert(DataMediaPairDO dataMediaPair) {
        Assert.assertNotNull(dataMediaPair);
        getSqlMapClientTemplate().insert("insertDataMediaPair", dataMediaPair);
        return dataMediaPair;
    }

    public void insertColumnPairs(List<ColumnPair> ColumnPairs) {
        Assert.assertNotNull(ColumnPairs);
        getSqlMapClientTemplate().insert("insertColumnPairs", ColumnPairs);
    }

    public void delete(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);
        getSqlMapClientTemplate().delete("deleteDataMediaPairById", dataMediaPairId);
    }

    public void update(DataMediaPairDO dataMediaPair) {
        Assert.assertNotNull(dataMediaPair);
        getSqlMapClientTemplate().update("updateDataMediaPair", dataMediaPair);
    }

    public boolean checkUnique(DataMediaPairDO dataMediaPair) {
        int count = (Integer) getSqlMapClientTemplate().queryForObject("checkDataMediaPairUnique", dataMediaPair);
        return count == 0 ? true : false;
    }

    public DataMediaPairDO findById(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);
        return (DataMediaPairDO) getSqlMapClientTemplate().queryForObject("findDataMediaPairById", dataMediaPairId);
    }

    public List<DataMediaPairDO> listAll() {

        return (List<DataMediaPairDO>) getSqlMapClientTemplate().queryForList("listDataMediaPairs");
    }

    public List<DataMediaPairDO> listByPipelineId(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        return (List<DataMediaPairDO>) getSqlMapClientTemplate().queryForList("listDataMediaPairsByPipelineId",
                                                                              pipelineId);
    }

    public List<DataMediaPairDO> listByCondition(Map condition) {
        List<DataMediaPairDO> dataMediaPairDos = getSqlMapClientTemplate().queryForList("listDataMediaPairs", condition);
        return dataMediaPairDos;
    }

    public List<DataMediaPairDO> listByDataMediaId(Long dataMediaId) {
        Assert.assertNotNull(dataMediaId);
        return (List<DataMediaPairDO>) getSqlMapClientTemplate().queryForList("listDataMediaPairsByDataMediaId",
                                                                              dataMediaId);
    }

    public List<DataMediaPairDO> listByMultiId(Long... identities) {
        List<DataMediaPairDO> dataMediaPairDos = getSqlMapClientTemplate().queryForList("listDataMediaPairByIds",
                                                                                        identities);
        return dataMediaPairDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getDataMediaPairCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getDataMediaPairCount", condition);
        return count.intValue();
    }

}
