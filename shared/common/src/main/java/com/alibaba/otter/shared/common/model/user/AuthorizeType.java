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

package com.alibaba.otter.shared.common.model.user;

/**
 * 用户权限
 * 
 * @author simon 2011-11-10 下午07:34:58
 */
public enum AuthorizeType {
    /** 匿名用户 */
    ANONYMOUS,
    /** 普通操作员 */
    OPERATOR,
    /** 系统管理员 */
    ADMIN;

    public boolean isAnonymous() {
        return this.equals(AuthorizeType.ANONYMOUS);
    }

    public boolean isOperator() {
        return this.equals(AuthorizeType.OPERATOR);
    }

    public boolean isAdmin() {
        return this.equals(AuthorizeType.ADMIN);
    }

}
