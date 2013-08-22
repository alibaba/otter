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

package com.alibaba.otter.manager.biz.config.datamediasource.dal.ibatis;

import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.datamediasource.dal.DataMediaSourceDAO;
import com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO;

/**
 * DataMediaSource的DAO层，ibatis的实现，主要是CRUD操作。
 * 
 * @author simon
 */
public class IbatisDataMediaSourceDAO extends SqlMapClientDaoSupport implements DataMediaSourceDAO {

    public DataMediaSourceDO insert(DataMediaSourceDO dataMediaSourceDO) {
        Assert.assertNotNull(dataMediaSourceDO);
        getSqlMapClientTemplate().insert("insertDataMediaSource", dataMediaSourceDO);
        return dataMediaSourceDO;
    }

    public void delete(Long dataMediaSourceId) {
        Assert.assertNotNull(dataMediaSourceId);
        getSqlMapClientTemplate().delete("deleteDataMediaSourceById", dataMediaSourceId);
    }

    public void update(DataMediaSourceDO dataMediaSourceDO) {
        Assert.assertNotNull(dataMediaSourceDO);
        getSqlMapClientTemplate().update("updateDataMediaSource", dataMediaSourceDO);
    }

    public boolean checkUnique(DataMediaSourceDO dataMediaSourceDO) {
        int count = (Integer) getSqlMapClientTemplate().queryForObject("checkDataMediaSourceUnique", dataMediaSourceDO);
        return count == 0 ? true : false;
    }

    public DataMediaSourceDO findById(Long dataMediaSourceId) {
        Assert.assertNotNull(dataMediaSourceId);
        return (DataMediaSourceDO) getSqlMapClientTemplate().queryForObject("findDataMediaSourceById",
                                                                            dataMediaSourceId);
    }

    public List<DataMediaSourceDO> listByCondition(Map condition) {
        List<DataMediaSourceDO> dataMediaSourceDos = getSqlMapClientTemplate().queryForList("listDataMediaSources",
                                                                                            condition);
        return dataMediaSourceDos;
    }

    public List<DataMediaSourceDO> listAll() {

        return (List<DataMediaSourceDO>) getSqlMapClientTemplate().queryForList("listDataMediaSources");
    }

    public List<DataMediaSourceDO> listByMultiId(Long... identities) {
        List<DataMediaSourceDO> dataMediaSourceDos = getSqlMapClientTemplate().queryForList("listSourceByIds",
                                                                                            identities);
        return dataMediaSourceDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getSourceCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getSourceCount", condition);
        return count.intValue();
    }

}
