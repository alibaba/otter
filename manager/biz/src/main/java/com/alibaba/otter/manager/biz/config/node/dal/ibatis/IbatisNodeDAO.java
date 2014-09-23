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

package com.alibaba.otter.manager.biz.config.node.dal.ibatis;

import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.node.dal.NodeDAO;
import com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO;

/**
 * Node的DAO层，ibatis的实现，主要是CRUD操作。
 * 
 * @author simon
 */
public class IbatisNodeDAO extends SqlMapClientDaoSupport implements NodeDAO {

    public NodeDO insert(NodeDO node) {
        Assert.assertNotNull(node);
        getSqlMapClientTemplate().insert("insertNode", node);
        return node;
    }

    public void delete(Long nodeId) {
        Assert.assertNotNull(nodeId);
        getSqlMapClientTemplate().delete("deleteNodeById", nodeId);
    }

    public void update(NodeDO node) {
        Assert.assertNotNull(node);
        getSqlMapClientTemplate().update("updateNode", node);
    }

    public boolean checkUnique(NodeDO node) {
        int count = (Integer) getSqlMapClientTemplate().queryForObject("checkNodeUnique", node);
        return count == 0 ? true : false;
    }

    public List<NodeDO> listByCondition(Map condition) {
        List<NodeDO> nodeDos = getSqlMapClientTemplate().queryForList("listNodes", condition);
        return nodeDos;
    }

    public NodeDO findById(Long nodeId) {
        Assert.assertNotNull(nodeId);
        return (NodeDO) getSqlMapClientTemplate().queryForObject("findNodeById", nodeId);
    }

    public List<NodeDO> listAll() {
        return (List<NodeDO>) getSqlMapClientTemplate().queryForList("listNodes");
    }

    public List<NodeDO> listByMultiId(Long... identities) {
        List<NodeDO> nodeDos = getSqlMapClientTemplate().queryForList("listNodeByIds", identities);
        return nodeDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getNodeCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getNodeCount", condition);
        return count.intValue();
    }

}
