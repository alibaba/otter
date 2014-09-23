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
