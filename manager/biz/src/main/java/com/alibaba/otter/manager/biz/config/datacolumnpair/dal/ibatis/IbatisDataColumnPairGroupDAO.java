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

package com.alibaba.otter.manager.biz.config.datacolumnpair.dal.ibatis;

import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.datacolumnpair.dal.DataColumnPairGroupDAO;
import com.alibaba.otter.manager.biz.config.datacolumnpair.dal.dataobject.DataColumnPairGroupDO;

/**
 * 类IbatisDataColumnPairGroupDAO.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-4-20 下午4:11:06
 */
public class IbatisDataColumnPairGroupDAO extends SqlMapClientDaoSupport implements DataColumnPairGroupDAO {

    public DataColumnPairGroupDO insert(DataColumnPairGroupDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlMapClientTemplate().insert("insertDataColumnPairGroup", entityObj);
        return entityObj;
    }

    public void delete(Long identity) {
        // TODO Auto-generated method stub

    }

    public void deleteByDataMediaPairId(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);
        getSqlMapClientTemplate().delete("deleteDataColumnPairGroupByDataMediaPairId", dataMediaPairId);

    }

    public void update(DataColumnPairGroupDO entityObj) {
        // TODO Auto-generated method stub

    }

    public List<DataColumnPairGroupDO> listAll() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DataColumnPairGroupDO> ListByDataMediaPairId(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);
        List<DataColumnPairGroupDO> dataColumnPairGroupDos = getSqlMapClientTemplate().queryForList("listDataColumnPairGroupByDataMediaPairId",
                                                                                                    dataMediaPairId);
        return dataColumnPairGroupDos;
    }

    public List<DataColumnPairGroupDO> ListByDataMediaPairIds(Long... dataMediaPairIds) {
        Assert.assertNotNull(dataMediaPairIds);
        List<DataColumnPairGroupDO> dataColumnPairGroupDos = getSqlMapClientTemplate().queryForList("listDataColumnPairGroupByDataMediaPairIds",
                                                                                                    dataMediaPairIds);
        return dataColumnPairGroupDos;
    }

    public List<DataColumnPairGroupDO> listByCondition(Map condition) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DataColumnPairGroupDO> listByMultiId(Long... identities) {
        // TODO Auto-generated method stub
        return null;
    }

    public DataColumnPairGroupDO findById(Long identity) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getCount(Map condition) {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean checkUnique(DataColumnPairGroupDO entityObj) {
        // TODO Auto-generated method stub
        return false;
    }

}
