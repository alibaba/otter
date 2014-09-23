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

package com.alibaba.otter.manager.biz.user.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.shared.common.model.user.User;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.user.UserService;
import com.alibaba.otter.manager.biz.user.dal.UserDAO;
import com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO;

/**
 * TODO Comment of UserServiceImpl
 * 
 * @author simon
 */
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private UserDAO             userDao;

    public void createUser(User user) {
        Assert.assertNotNull(user);
        try {
            UserDO userDo = userDao.insertUser(modelToDo(user));
            if (userDo.getId() == 0) {
                String exceptionCause = "exist the same name user in the database.";
                logger.warn("WARN ## " + exceptionCause);
                throw new RepeatConfigureException(exceptionCause);
            }
        } catch (RepeatConfigureException rce) {
            throw rce;
        } catch (Exception e) {
            logger.error("ERROR ## create user has an exception!");
            throw new ManagerException(e);
        }
    }

    public void deleteUser(Long userId) {
        Assert.assertNotNull(userId);
        userDao.deleteUser(userId);
    }

    public void updataUser(User user) {
        Assert.assertNotNull(user);
        try {
            UserDO UserDo = modelToDo(user);
            if (userDao.chackUnique(UserDo)) {
                userDao.updateUser(UserDo);
            } else {
                String exceptionCause = "exist the same name user in the database.";
                logger.warn("WARN ## " + exceptionCause);
                throw new RepeatConfigureException(exceptionCause);
            }
        } catch (RepeatConfigureException rce) {
            throw rce;
        } catch (Exception e) {
            logger.error("ERROR ## create user has an exception!");
            throw new ManagerException(e);
        }
    }

    public User findUserById(Long userId) {
        Assert.assertNotNull(userId);
        return doToModel(userDao.findUserById(userId));
    }

    public List<User> ListAllUsers() {
        List<UserDO> userDos = userDao.listAllUsers();
        List<User> users = new ArrayList<User>();
        for (UserDO userDo : userDos) {
            users.add(doToModel(userDo));
        }
        return users;
    }

    public List<User> listByCondition(Map condition) {
        List<UserDO> userDos = userDao.listByCondition(condition);
        List<User> users = new ArrayList<User>();
        for (UserDO userDo : userDos) {
            users.add(doToModel(userDo));
        }
        return users;
    }

    /**
     * 拿到user总数进行分页
     */
    public int getCount() {
        return userDao.getCount();
    }

    public int getCount(Map condition) {
        return userDao.getCount(condition);
    }

    @Override
    public User login(String name, String password) {
        UserDO userDo = userDao.getAuthenticatedUser(name, password);
        if (null == userDo) {
            return null;
        }
        return doToModel(userDo);
    }

    private User doToModel(UserDO userDo) {
        User user = new User();
        user.setId(userDo.getId());
        user.setName(userDo.getName());
        user.setDepartment(userDo.getDepartment());
        user.setRealName(userDo.getRealName());
        user.setAuthorizeType(userDo.getAuthorizeType());
        user.setGmtCreate(userDo.getGmtCreate());
        user.setGmtModified(userDo.getGmtModified());
        return user;
    }

    private UserDO modelToDo(User user) {
        UserDO userDo = new UserDO();
        userDo.setId(user.getId());
        userDo.setName(user.getName());
        userDo.setPassword(user.getPassword());
        userDo.setDepartment(user.getDepartment());
        userDo.setRealName(user.getRealName());
        userDo.setAuthorizeType(user.getAuthorizeType());
        userDo.setGmtCreate(user.getGmtCreate());
        userDo.setGmtModified(user.getGmtModified());
        return userDo;
    }

    /* ------------------------setter / getter--------------------------- */

    public UserDAO getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDAO userDao) {
        this.userDao = userDao;
    }
}
