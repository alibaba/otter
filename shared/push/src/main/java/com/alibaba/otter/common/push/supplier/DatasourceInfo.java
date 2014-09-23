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

package com.alibaba.otter.common.push.supplier;

import java.net.InetSocketAddress;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author zebin.xuzb 2013-1-23 下午4:42:48
 * @since 4.1.3
 */
public class DatasourceInfo {

    private InetSocketAddress address;            // 主库信息
    private String            username;           // 帐号
    private String            password;           // 密码
    private String            defaultDatabaseName; // 默认链接的数据库

    public DatasourceInfo(){
        super();
    }

    public DatasourceInfo(InetSocketAddress address, String username, String password){
        this(address, username, password, "");
    }

    public DatasourceInfo(InetSocketAddress address, String username, String password, String defaultDatabaseName){
        this.address = address;
        this.username = username;
        this.password = password;
        this.defaultDatabaseName = defaultDatabaseName;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDefaultDatabaseName() {
        return defaultDatabaseName;
    }

    public void setDefaultDatabaseName(String defaultDatabaseName) {
        this.defaultDatabaseName = defaultDatabaseName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
