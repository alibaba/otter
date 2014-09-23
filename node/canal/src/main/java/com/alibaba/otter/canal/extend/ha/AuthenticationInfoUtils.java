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

package com.alibaba.otter.canal.extend.ha;

import org.apache.commons.beanutils.BeanUtils;

import com.alibaba.otter.canal.common.CanalException;
import com.alibaba.otter.canal.parse.support.AuthenticationInfo;
import com.alibaba.otter.common.push.supplier.DatasourceInfo;

/**
 * @author zebin.xuzb 2013-1-23 下午4:54:33
 * @since 4.1.3
 */
public abstract class AuthenticationInfoUtils {

    private AuthenticationInfoUtils(){
    }

    public static AuthenticationInfo createFrom(DatasourceInfo datasourceInfo) {
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        try {
            BeanUtils.copyProperties(authenticationInfo, datasourceInfo);
        } catch (Exception e) {
            throw new CanalException(e);
        }
        return authenticationInfo;
    }

}
