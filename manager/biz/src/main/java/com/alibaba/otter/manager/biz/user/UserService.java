package com.alibaba.otter.manager.biz.user;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.shared.common.model.user.User;

/**
 * @author simon
 */
public interface UserService {

    public void createUser(User user);

    public void deleteUser(Long userId);

    public void updataUser(User user);

    public User findUserById(Long userId);

    public User login(String name, String password);

    public List<User> ListAllUsers();

    public List<User> listByCondition(Map condition);

    public int getCount();

    public int getCount(Map condition);
}
