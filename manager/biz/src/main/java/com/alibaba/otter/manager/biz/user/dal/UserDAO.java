package com.alibaba.otter.manager.biz.user.dal;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO;

/**
 * TODO Comment of UserDAO
 * 
 * @author simon
 */
public interface UserDAO {

    public UserDO findUserById(Long userId);

    public UserDO insertUser(UserDO user);

    public void updateUser(UserDO user);

    public void deleteUser(Long userId);

    public List<UserDO> listAllUsers();

    public List<UserDO> listByCondition(Map condition);

    public UserDO getAuthenticatedUser(String name, String password);

    public boolean chackUnique(UserDO user);

    public int getCount();

    public int getCount(Map condition);
}
