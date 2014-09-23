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

package com.alibaba.otter.manager.web.webx.valve.auth;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.otter.shared.common.model.user.AuthorizeType;

/**
 * 基于正则的URL匹配
 * 
 * @author jianghang 2011-11-11 下午12:24:38
 * @version 4.0.0
 */

public class RegExpURLAnalyze {

    private AuthorizeProtected anonymous;
    private AuthorizeProtected operator;
    private AuthorizeProtected admin;

    public List<AuthorizeType> check(String requestUrl) {
        List<AuthorizeType> result = new ArrayList<AuthorizeType>();
        if (anonymous != null && anonymous.getUrlProtected() != null && anonymous.getUrlProtected().check(requestUrl)) {
            result.add(AuthorizeType.ANONYMOUS);
        }

        if (operator != null && operator.getUrlProtected() != null && operator.getUrlProtected().check(requestUrl)) {
            result.add(AuthorizeType.OPERATOR);
        }

        if (admin != null && admin.getUrlProtected() != null && admin.getUrlProtected().check(requestUrl)) {
            result.add(AuthorizeType.ADMIN);
        }
        return result;
    }

    public List<AuthorizeType> check(String action, String method) {
        List<AuthorizeType> result = new ArrayList<AuthorizeType>();
        if (anonymous != null && anonymous.getActionProtected() != null
            && anonymous.getActionProtected().check(action, method)) {
            result.add(AuthorizeType.ANONYMOUS);
        }

        if (operator != null && operator.getActionProtected() != null
            && operator.getActionProtected().check(action, method)) {
            result.add(AuthorizeType.OPERATOR);
        }

        if (admin != null && admin.getActionProtected() != null && admin.getActionProtected().check(action, method)) {
            result.add(AuthorizeType.ADMIN);
        }
        return result;
    }

    public void setAnonymous(AuthorizeProtected anonymous) {
        this.anonymous = anonymous;
    }

    public void setOperator(AuthorizeProtected operator) {
        this.operator = operator;
    }

    public void setAdmin(AuthorizeProtected admin) {
        this.admin = admin;
    }

}
