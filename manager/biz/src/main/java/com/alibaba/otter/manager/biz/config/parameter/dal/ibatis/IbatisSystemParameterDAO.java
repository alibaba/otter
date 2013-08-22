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

package com.alibaba.otter.manager.biz.config.parameter.dal.ibatis;

import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.parameter.dal.SystemParameterDAO;
import com.alibaba.otter.manager.biz.config.parameter.dal.dataobject.SystemParameterDO;

/**
 * SystemParameter的DAO层，ibatis的实现，主要是CRUD操作。
 * 
 * @author sarah.lij 2012-4-13 下午04:57:52
 */
public class IbatisSystemParameterDAO extends SqlMapClientDaoSupport implements SystemParameterDAO {

    public SystemParameterDO insert(SystemParameterDO systemParameter) {
        Assert.assertNotNull(systemParameter);
        getSqlMapClientTemplate().insert("insertParameter", systemParameter);
        return systemParameter;
    }

    public void update(SystemParameterDO systemParameter) {
        throw new UnsupportedOperationException();
    }

    public void delete(Long parameterId) {
        throw new UnsupportedOperationException();
    }

    public boolean checkUnique(SystemParameterDO systemParameter) {
        throw new UnsupportedOperationException();
    }

    public SystemParameterDO findById(Long parameterId) {
        throw new UnsupportedOperationException();
    }

    public int getCount() {
        throw new UnsupportedOperationException();
    }

    public int getCount(Map condition) {
        throw new UnsupportedOperationException();
    }

    public List<SystemParameterDO> listAll() {
        return (List<SystemParameterDO>) getSqlMapClientTemplate().queryForList("listParameters");
    }

    public List<SystemParameterDO> listByCondition(Map condition) {
        throw new UnsupportedOperationException();
    }

    public List<SystemParameterDO> listByMultiId(Long... identities) {
        throw new UnsupportedOperationException();
    }

}
