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

package com.alibaba.otter.manager.biz.config.datamatrix.dal.ibatis;

import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.datamatrix.dal.DataMatrixDAO;
import com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO;

public class IbatisDataMatrixDAO extends SqlMapClientDaoSupport implements DataMatrixDAO {

    public DataMatrixDO insert(DataMatrixDO matrixDo) {
        Assert.assertNotNull(matrixDo);
        getSqlMapClientTemplate().insert("insertDataMatrix", matrixDo);
        return matrixDo;
    }

    public void delete(Long matrixId) {
        Assert.assertNotNull(matrixId);
        getSqlMapClientTemplate().delete("deleteDataMatrixById", matrixId);
    }

    public void update(DataMatrixDO matrixDo) {
        Assert.assertNotNull(matrixDo);
        getSqlMapClientTemplate().update("updateDataMatrix", matrixDo);
    }

    public List<DataMatrixDO> listAll() {
        return (List<DataMatrixDO>) getSqlMapClientTemplate().queryForList("listDataMatrixs");
    }

    public List<DataMatrixDO> listByMultiId(Long... identities) {
        List<DataMatrixDO> DataMatrixDOs = getSqlMapClientTemplate().queryForList("listDataMatrixByIds", identities);
        return DataMatrixDOs;
    }

    public boolean checkUnique(DataMatrixDO matrixDo) {
        int count = (Integer) getSqlMapClientTemplate().queryForObject("checkDataMatrixUnique", matrixDo);
        return count == 0 ? true : false;
    }

    public DataMatrixDO findByGroupKey(String groupKey) {
        Assert.assertNotNull(groupKey);
        return (DataMatrixDO) getSqlMapClientTemplate().queryForObject("findDataMatrixByGroupKey", groupKey);
    }

    public DataMatrixDO findById(Long identity) {
        throw new UnsupportedOperationException();
    }

    public int getCount() {
        return 0;
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getDataMatrixCount", condition);
        return count.intValue();
    }

    public List<DataMatrixDO> listByCondition(Map condition) {
        List<DataMatrixDO> DataMatrixDOs = getSqlMapClientTemplate().queryForList("listDataMatrixs", condition);
        return DataMatrixDOs;
    }
}
