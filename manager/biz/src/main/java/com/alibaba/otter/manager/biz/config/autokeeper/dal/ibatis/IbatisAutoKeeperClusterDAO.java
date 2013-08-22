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

package com.alibaba.otter.manager.biz.config.autokeeper.dal.ibatis;

import java.util.List;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.autokeeper.dal.AutoKeeperClusterDAO;
import com.alibaba.otter.manager.biz.config.autokeeper.dal.dataobject.AutoKeeperClusterDO;

/**
 * 类IbatisAutoKeeperClusterDAO.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-9-24 下午5:17:17
 * @version 4.1.0
 */
public class IbatisAutoKeeperClusterDAO extends SqlMapClientDaoSupport implements AutoKeeperClusterDAO {

    @Override
    public AutoKeeperClusterDO findAutoKeeperClusterById(Long id) {
        Assert.assertNotNull(id);
        return (AutoKeeperClusterDO) getSqlMapClientTemplate().queryForObject("findAutoKeeperClusterById", id);
    }

    @Override
    public List<AutoKeeperClusterDO> listAutoKeeperClusters() {
        List<AutoKeeperClusterDO> autoKeeperClusterDOs = getSqlMapClientTemplate().queryForList("listAutoKeeperClusters");
        return autoKeeperClusterDOs;
    }

    @Override
    public void updateAutoKeeperCluster(AutoKeeperClusterDO autoKeeperClusterDo) {
        Assert.assertNotNull(autoKeeperClusterDo);
        getSqlMapClientTemplate().update("updateAutoKeeperCluster", autoKeeperClusterDo);
    }

    @Override
    public void insertAutoKeeperClusterDO(AutoKeeperClusterDO autoKeeperClusterDo) {
        Assert.assertNotNull(autoKeeperClusterDo);
        getSqlMapClientTemplate().insert("insertAutoKeeperCluster", autoKeeperClusterDo);
    }

    @Override
    public void delete(Long clusterId) {
        Assert.assertNotNull(clusterId);
        getSqlMapClientTemplate().insert("deleteAutoKeeperClusterById", clusterId);
    }

    @Override
    public Integer getCount() {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getAutoKeeperClusterCount");
        return count.intValue();
    }

}
