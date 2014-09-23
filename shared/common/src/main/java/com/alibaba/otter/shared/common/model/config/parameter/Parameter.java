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

package com.alibaba.otter.shared.common.model.config.parameter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参数信息类
 * 
 * @author jianghang 2011-9-2 上午10:35:16
 */
public class Parameter implements Serializable {

    private static final long   serialVersionUID      = -8019445081834822960L;
    private Map<String, String> params                = new ConcurrentHashMap<String, String>(10);
    private boolean             mergeSystemProperties = true;

    public Object put(String key, String value) {
        return params.put(key, value);
    }

    public String getString(String key) {
        String value = params.get(key);
        // 是否需要合并system.properties
        return (mergeSystemProperties && value == null) ? System.getProperty(key) : null;
    }

    public Integer getInteger(String key) {
        String value = getString(key);
        return value == null ? null : Integer.valueOf(value);
    }

    public Long getLong(String key) {
        String value = getString(key);
        return value == null ? null : Long.valueOf(value);
    }

    public BigDecimal getBigDecimal(String key) {
        String value = getString(key);
        return value == null ? null : new BigDecimal(value);
    }

    public Boolean getBool(String key) {
        String value = getString(key);
        return value == null ? null : Boolean.valueOf(value);
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> enumType) {
        String value = getString(key);
        return (T) Enum.valueOf(enumType, value);
    }

    /**
     * 设置对应的参数对象
     */
    protected void setParams(Map params) {
        params.putAll(params);
    }

    /**
     * 获取对应的params对象
     */
    protected Map getParams() {
        return params;
    }
}
