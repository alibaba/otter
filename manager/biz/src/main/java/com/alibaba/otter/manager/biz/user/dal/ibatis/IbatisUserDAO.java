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

package com.alibaba.otter.manager.biz.user.dal.ibatis;

import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.user.dal.UserDAO;
import com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO;

/**
 * TODO Comment of IbatisUserDAO
 * 
 * @author simon
 */
public class IbatisUserDAO extends SqlMapClientDaoSupport implements UserDAO {

    public UserDO findUserById(Long userId) {
        Assert.assertNotNull(userId);
        return (UserDO) getSqlMapClientTemplate().queryForObject("findUserById", userId);
    }

    public List<UserDO> listAllUsers() {
        return (List<UserDO>) getSqlMapClientTemplate().queryForList("listUsers");
    }

    public List<UserDO> listByCondition(Map condition) {
        return (List<UserDO>) getSqlMapClientTemplate().queryForList("listUsers", condition);
    }

    public UserDO insertUser(UserDO user) {
        Assert.assertNotNull(user);
        getSqlMapClientTemplate().insert("insertUser", user);
        return user;
    }

    public void updateUser(UserDO user) {
        Assert.assertNotNull(user);
        getSqlMapClientTemplate().update("updateUser", user);
    }

    public boolean chackUnique(UserDO user) {
        Assert.assertNotNull(user);
        int count = (Integer) getSqlMapClientTemplate().queryForObject("checkUserUnique", user);
        return count == 0 ? true : false;
    }

    public void deleteUser(Long userId) {
        Assert.assertNotNull(userId);
        getSqlMapClientTemplate().delete("deleteUserById", userId);
    }

    public UserDO getAuthenticatedUser(String name, String password) {
        UserDO userDo = new UserDO();

        userDo.setName(name);
        userDo.setPassword(password);

        return (UserDO) getSqlMapClientTemplate().queryForObject("getUserByNameAndPassword", userDo);
    }

    public int getCount() {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getUserCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getUserCount", condition);
        return count.intValue();
    }

}
