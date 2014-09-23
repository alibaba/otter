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

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.datacolumnpair.dal.DataColumnPairDAO;
import com.alibaba.otter.manager.biz.config.datacolumnpair.dal.dataobject.DataColumnPairDO;
import com.alibaba.otter.manager.biz.config.datacolumnpair.dal.dataobject.DataColumnPairGroupDO;

/**
 * 类IbatisDataColumnPairDAO.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-4-20 下午4:10:48
 */
public class IbatisDataColumnPairDAO extends SqlMapClientDaoSupport implements DataColumnPairDAO {

    public DataColumnPairGroupDO insert(DataColumnPairGroupDO dataColumnPairDo) {
        Assert.assertNotNull(dataColumnPairDo);
        getSqlMapClientTemplate().insert("insertDataColumnPair", dataColumnPairDo);
        return dataColumnPairDo;
    }

    public void insertBatch(List<DataColumnPairDO> dataColumnPairDos) {
        try {
            getSqlMapClientTemplate().getSqlMapClient().startBatch();

            Iterator it = dataColumnPairDos.iterator();
            while (it.hasNext()) {
                DataColumnPairDO dataColumnPairDo = (DataColumnPairDO) it.next();
                getSqlMapClientTemplate().getSqlMapClient().insert("insertDataColumnPair", dataColumnPairDo);
            }

            getSqlMapClientTemplate().getSqlMapClient().executeBatch();

        } catch (SQLException ex1) {
            ex1.printStackTrace();
        }

    }

    public void delete(Long dataColumnPairId) {
        Assert.assertNotNull(dataColumnPairId);
        getSqlMapClientTemplate().delete("deleteDataColumnPairById", dataColumnPairId);
    }

    public void deleteByDataMediaPairId(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);
        getSqlMapClientTemplate().delete("deleteDataColumnPairByDataMediaPairId", dataMediaPairId);
    }

    public void update(DataColumnPairGroupDO dataColumnPairDo) {
        Assert.assertNotNull(dataColumnPairDo);
        getSqlMapClientTemplate().update("updateDataColumnPair", dataColumnPairDo);

    }

    public List<DataColumnPairDO> listAll() {
        List<DataColumnPairDO> dataColumnPairGroupDos = getSqlMapClientTemplate().queryForList("listDataColumnPairs");
        return dataColumnPairGroupDos;
    }

    public List<DataColumnPairDO> listByCondition(Map condition) {
        return null;
    }

    public List<DataColumnPairDO> listByMultiId(Long... identities) {
        return null;
    }

    public DataColumnPairDO findById(Long identity) {
        Assert.assertNotNull(identity);
        return (DataColumnPairDO) getSqlMapClientTemplate().queryForObject("findDataColumnPairById", identity);
    }

    public int getCount() {
        return 0;
    }

    public int getCount(Map condition) {
        return 0;
    }

    public boolean checkUnique(DataColumnPairGroupDO entityObj) {
        return false;
    }

    public List<DataColumnPairDO> listByDataMediaPairId(Long dataMediaPairId) {
        List<DataColumnPairDO> dataColumnPairDos = getSqlMapClientTemplate().queryForList("listDataColumnPairByDataMediaPairId",
                                                                                          dataMediaPairId);
        return dataColumnPairDos;
    }

    public List<DataColumnPairDO> listByDataMediaPairIds(Long... dataMediaPairIds) {
        List<DataColumnPairDO> dataColumnPairDos = getSqlMapClientTemplate().queryForList("listDataColumnPairByDataMediaPairIds",
                                                                                          dataMediaPairIds);
        return dataColumnPairDos;
    }

    public DataColumnPairDO insert(DataColumnPairDO entityObj) {
        // TODO Auto-generated method stub
        return null;
    }

    public void update(DataColumnPairDO entityObj) {
        // TODO Auto-generated method stub

    }

    public boolean checkUnique(DataColumnPairDO entityObj) {
        // TODO Auto-generated method stub
        return false;
    }

}
