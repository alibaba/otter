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

package com.alibaba.otter.common.push.datasource;

import com.alibaba.otter.shared.common.model.config.data.DataMediaType;

/**
 * @author zebin.xuzb 2013-1-23 下午6:46:26
 * @since 4.1.3
 */
public class DataSourceKey {

    private String        url;
    private String        userName;
    private String        password;
    private String        driverClassName;
    private DataMediaType dataMediaType;
    private String        encoding;

    public static DataSourceKey getInstance(String url, String userName, String password, String driverClassName,
                                            DataMediaType dataMediaType, String encoding) {
        return new DataSourceKey(url, userName, password, driverClassName, dataMediaType, encoding);
    }

    public DataSourceKey(String url, String userName, String password, String driverClassName,
                         DataMediaType dataMediaType, String encoding){
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.driverClassName = driverClassName;
        this.dataMediaType = dataMediaType;
        this.encoding = encoding;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataMediaType == null) ? 0 : dataMediaType.hashCode());
        result = prime * result + ((driverClassName == null) ? 0 : driverClassName.hashCode());
        result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DataSourceKey other = (DataSourceKey) obj;
        if (dataMediaType != other.dataMediaType) return false;
        if (driverClassName == null) {
            if (other.driverClassName != null) return false;
        } else if (!driverClassName.equals(other.driverClassName)) return false;
        if (encoding == null) {
            if (other.encoding != null) return false;
        } else if (!encoding.equals(other.encoding)) return false;
        if (password == null) {
            if (other.password != null) return false;
        } else if (!password.equals(other.password)) return false;
        if (url == null) {
            if (other.url != null) return false;
        } else if (!url.equals(other.url)) return false;
        if (userName == null) {
            if (other.userName != null) return false;
        } else if (!userName.equals(other.userName)) return false;
        return true;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public DataMediaType getDataMediaType() {
        return dataMediaType;
    }

    public String getEncoding() {
        return encoding;
    }

}
