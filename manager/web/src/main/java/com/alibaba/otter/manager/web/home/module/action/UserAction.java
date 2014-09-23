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

package com.alibaba.otter.manager.web.home.module.action;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import com.alibaba.citrus.service.form.CustomErrors;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.service.requestcontext.parser.ParameterParser;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.user.UserService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.user.User;
import com.alibaba.otter.shared.common.utils.SecurityUtils;

/**
 * 类UserAction.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-11-10 下午07:14:50
 */
public class UserAction extends AbstractAction {

    @Resource(name = "userService")
    private UserService userService;

    public void doAdd(@FormGroup("addUserInfo") Group userInfo, Navigator nav,
                      @FormField(name = "formUserError", group = "addUserInfo") CustomErrors err) {
        User user = new User();
        userInfo.setProperties(user);
        user.setPassword(SecurityUtils.getPassword(user.getPassword()));
        try {
            userService.createUser(user);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidUser");
            return;
        }
        nav.redirectTo(WebConstant.USER_MANAGER_LINK);

    }

    public void doEdit(@FormGroup("editUserInfo") Group userInfo, @Param("pageIndex") int pageIndex,
                       @Param("searchKey") String searchKey, Navigator nav,
                       @FormField(name = "formUserError", group = "editUserInfo") CustomErrors err) {
        User user = new User();
        userInfo.setProperties(user);
        if (null != user.getPassword()) {
            if (user.getPassword().length() < 6) {
                err.setMessage("passwordTooLess");
                return;
            }
            user.setPassword(SecurityUtils.getPassword(user.getPassword()));
        }

        try {
            userService.updataUser(user);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidUser");
            return;
        }
        nav.redirectToLocation("userManager.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

    public void doDelete(@Param("userId") Long userId, @Param("pageIndex") int pageIndex,
                         @Param("searchKey") String searchKey, Navigator nav) throws WebxException {
        userService.deleteUser(userId);
        nav.redirectToLocation("userManager.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

    public void doLogin(@FormGroup("login") User user,
                        @FormField(name = "loginError", group = "login") CustomErrors err, @Param("Done") String url,
                        Navigator nav, HttpSession session, ParameterParser params) throws Exception {

        user = userService.login(user.getName(), SecurityUtils.getPassword(user.getPassword()));

        if (user != null) {
            // 在session中创建User对象
            session.setAttribute(WebConstant.USER_SESSION_KEY, user);

            // 跳转到return页面
            if (null == url) {
                nav.redirectTo(WebConstant.CHANNEL_LIST_LINK);
            } else {
                nav.redirectToLocation(url);
            }

        } else {
            err.setMessage("invalidUserOrPassword");
        }
    }

    public void doLogout(HttpSession session, Navigator nav, ParameterParser params) throws Exception {
        // 清除session中的user
        session.removeAttribute(WebConstant.USER_SESSION_KEY);

        // 跳转到return页面
        nav.redirectTo(WebConstant.OTTER_LOGIN_LINK);
    }

}
