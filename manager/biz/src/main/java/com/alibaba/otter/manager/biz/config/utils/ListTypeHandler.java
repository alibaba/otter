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

package com.alibaba.otter.manager.biz.config.utils;

import java.sql.SQLException;
import java.util.List;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

/**
 * 用于List数据结构的解析，ibatis相关
 * 
 * @author simon
 */
public class ListTypeHandler implements TypeHandlerCallback {

    @Override
    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        setter.setString(JsonUtils.marshalToString(parameter));
    }

    @Override
    public Object getResult(ResultGetter getter) throws SQLException {
        return JsonUtils.unmarshalFromString(getter.getString(), new TypeReference<List<Long>>() {
        });
    }

    public Object valueOf(String s) {
        return JsonUtils.unmarshalFromString(s, List.class);
    }

}
